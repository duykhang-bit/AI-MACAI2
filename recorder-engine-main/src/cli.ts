/**
 * @ai-test/recorder-engine — CLI
 * Record & Replay E2E tests via AI Agent prompts.
 *
 * Usage (tester paste prompt cho AI agent, agent gọi):
 *   frt-test record --app rsa-lab --env ci -n "Tạo đơn hàng"
 *   frt-test play --app rsa-lab --env ci --all
 *   frt-test scaffold-chain --name lab --apps "rsa-lab=https://..."
 */

import { Command } from 'commander';
import * as path from 'path';
import * as fs from 'fs';
import { execSync } from 'child_process';
import { FlowRunner } from './runner';
import { FlowRecorder } from './recorder';
import { FlowConverter } from './converter';
import { FlowReporter } from './reporter';
import { resolveBaseUrl, resolveEnv, loadConfig, isSchemaV2 } from './config';
import { registerScaffoldCommands } from './scaffold';

const FLOWS_DIR = path.join(process.cwd(), 'flows');
const REPORTS_DIR = path.join(process.cwd(), 'reports');

// ============================================================
// Version banner + staleness warning
// ============================================================
const ENGINE_DIR = path.resolve(__dirname, '..');
const PKG_VERSION = (() => {
  try {
    const pkg = JSON.parse(fs.readFileSync(path.join(ENGINE_DIR, 'package.json'), 'utf8'));
    return pkg.version || '0.0.0';
  } catch { return '0.0.0'; }
})();

function getGitInfo(): { hash: string; date: string; daysOld: number } {
  try {
    const hash = execSync('git log -1 --format=%h', { cwd: ENGINE_DIR, encoding: 'utf8' }).trim();
    const dateStr = execSync('git log -1 --format=%ci', { cwd: ENGINE_DIR, encoding: 'utf8' }).trim();
    const commitDate = new Date(dateStr);
    const daysOld = Math.floor((Date.now() - commitDate.getTime()) / 86400000);
    const shortDate = commitDate.toISOString().slice(0, 10);
    return { hash, date: shortDate, daysOld };
  } catch {
    return { hash: 'unknown', date: 'unknown', daysOld: 0 };
  }
}

function printVersionBanner(): void {
  const { hash, date, daysOld } = getGitInfo();
  const age = daysOld === 0 ? 'today' : `${daysOld} day${daysOld > 1 ? 's' : ''} old`;
  console.log(`@ai-test/recorder-engine v${PKG_VERSION} — commit ${hash} (${date}, ${age})`);
  if (daysOld > 14) {
    console.log(`⚠️  Engine local commit cũ ${daysOld} ngày. Chạy: cd ${ENGINE_DIR} && git pull`);
  }
}

/**
 * Compute flowsDir based on app + env:
 * - Schema v2: flows/{app}/{env}/
 * - Schema v1/v0: flows/{app}/ (no env subfolder)
 */
function computeFlowsDir(app?: string, env?: string): string {
  if (!app) return FLOWS_DIR;
  if (env && isSchemaV2()) {
    return path.join(FLOWS_DIR, app, env);
  }
  return path.join(FLOWS_DIR, app);
}

// ============================================================
// Program setup
// ============================================================
const program = new Command();

program
  .name('frt-test')
  .description('@ai-test/recorder-engine — Record & Replay E2E Tests')
  .version(PKG_VERSION, '-v, --version')
  .hook('preAction', () => {
    printVersionBanner();
    console.log('');
  });

// ============================================================
// RECORD command
// ============================================================
program
  .command('record')
  .description('Bắt đầu recording session — thao tác trên browser sẽ được ghi lại thành YAML')
  .requiredOption('-n, --name <name>', 'Tên flow (VD: "Tạo đơn hàng")')
  .option('-A, --app <app>', 'Target app (from config/environments.yaml)')
  .option('-E, --env <env>', 'Target environment (ci/uat/prod)')
  .option('-u, --url <url>', 'URL bắt đầu (override config)')
  .option('-d, --description <desc>', 'Mô tả flow')
  .option('-t, --tags <tags>', 'Tags (comma-separated)', 'recorded')
  .option('-a, --author <author>', 'Tên tester')
  .action(async (opts) => {
    const config = loadConfig();
    const app = opts.app || config.default_app;
    let baseUrl: string;
    let env: string | undefined;

    if (opts.url) {
      baseUrl = opts.url;
      env = opts.env;
    } else if (isSchemaV2()) {
      env = resolveEnv(opts.env);
      baseUrl = resolveBaseUrl(app, env);
    } else {
      baseUrl = resolveBaseUrl(app) || process.env.BASE_URL || 'http://localhost:3000';
      env = opts.env;
    }

    const outputDir = computeFlowsDir(app, env);
    fs.mkdirSync(outputDir, { recursive: true });

    console.log(`📍 App: ${app} | Env: ${env || 'default'} | URL: ${baseUrl}`);
    console.log('');

    const recorder = new FlowRecorder(outputDir);
    await recorder.start({
      url: baseUrl,
      flowName: opts.name,
      description: opts.description,
      tags: opts.tags.split(',').map((t: string) => t.trim()),
      author: opts.author,
      app,
      env,
    });
  });

// ============================================================
// PLAY command
// ============================================================
program
  .command('play [flowFile]')
  .description('Chạy 1 flow YAML hoặc tất cả flows')
  .option('-A, --app <app>', 'Target app (from config/environments.yaml)')
  .option('-E, --env <env>', 'Target environment (ci/uat/prod)')
  .option('--all', 'Chạy tất cả flows')
  .option('--tag <tag>', 'Chạy flows theo tag')
  .option('--headless', 'Chạy headless (không hiện browser)')
  .option('--slow <ms>', 'Slow motion (ms giữa mỗi action)', '0')
  .option('--var <vars...>', 'Override variables (key=value)')
  .option('--html-report', 'Generate HTML report')
  .option('--json-report', 'Generate JSON report (machine-readable)')
  .action(async (flowFile, opts) => {
    const config = loadConfig();
    const app = opts.app || config.default_app;
    let baseUrl: string | undefined;
    let env: string | undefined;

    if (isSchemaV2()) {
      env = resolveEnv(opts.env);
      baseUrl = resolveBaseUrl(app, env);
    } else {
      baseUrl = resolveBaseUrl(app) || undefined;
      env = opts.env;
    }

    const flowsDir = computeFlowsDir(app, env);
    const runner = new FlowRunner(flowsDir, FLOWS_DIR);
    const reporter = new FlowReporter();

    const variables: Record<string, string> = {};
    if (opts.var) {
      for (const v of opts.var) {
        const [key, ...valueParts] = v.split('=');
        variables[key] = valueParts.join('=');
      }
    }

    console.log(`📍 App: ${app} | Env: ${env || 'default'} | URL: ${baseUrl || '(not set)'}`);
    console.log('');

    const runOptions = {
      headless: opts.headless || false,
      slowMo: parseInt(opts.slow) || 0,
      variables,
      meta: {
        app,
        env,
        baseUrl,
      },
    };

    let results;

    if (opts.all) {
      console.log(`🚀 Running all flows for app: ${app} @ ${env || 'default'}...\n`);
      results = await runner.runAll(runOptions);
    } else if (opts.tag) {
      console.log(`🏷️  Running flows with tag: ${opts.tag}\n`);
      results = await runner.runByTag(opts.tag, runOptions);
    } else if (flowFile) {
      const flowPath = path.isAbsolute(flowFile)
        ? flowFile
        : path.resolve(flowsDir, flowFile);
      console.log(`▶️  Running: ${flowFile}\n`);
      const result = await runner.run(flowPath, runOptions);
      results = [result];
    } else {
      console.error('❌ Specify a flow file, --all, or --tag <tag>');
      process.exit(1);
    }

    reporter.printResults(results);

    if (opts.htmlReport) {
      const reportPath = path.join(REPORTS_DIR, `report-${Date.now()}.html`);
      reporter.generateHtmlReport(results, reportPath);
    }

    if (opts.jsonReport) {
      fs.mkdirSync(REPORTS_DIR, { recursive: true });
      const totalDuration = results.reduce((sum, r) => sum + r.duration, 0);
      const report = {
        summary: {
          total: results.length,
          passed: results.filter(r => r.status === 'passed').length,
          failed: results.filter(r => r.status === 'failed').length,
          skipped: results.filter(r => r.status === 'skipped').length,
          duration_ms: totalDuration,
        },
        results,
      };
      const jsonPath = path.join(REPORTS_DIR, `report-${Date.now()}.json`);
      fs.writeFileSync(jsonPath, JSON.stringify(report, null, 2));
      console.log(`📊 JSON report: ${jsonPath}`);
    }

    const failed = results.filter(r => r.status === 'failed').length;
    if (failed > 0) process.exit(1);
  });

// ============================================================
// CONVERT command
// ============================================================
program
  .command('convert <inputFile>')
  .description('Convert Playwright .spec.ts file sang YAML flow')
  .option('-n, --name <name>', 'Tên flow')
  .option('-t, --tags <tags>', 'Tags (comma-separated)')
  .option('-a, --author <author>', 'Tên tester')
  .action((inputFile, opts) => {
    const converter = new FlowConverter();
    const inputPath = path.resolve(inputFile);

    if (!fs.existsSync(inputPath)) {
      console.error(`❌ File not found: ${inputPath}`);
      process.exit(1);
    }

    const outputPath = converter.convertFile(inputPath, FLOWS_DIR, {
      flowName: opts.name,
      tags: opts.tags?.split(',').map((t: string) => t.trim()),
      author: opts.author,
    });

    console.log(`✅ Converted: ${outputPath}`);
    console.log(`   ⚠️  Review YAML file and adjust selectors/waits as needed.`);
  });

// ============================================================
// LIST command
// ============================================================
program
  .command('list')
  .description('Liệt kê tất cả flows đã có')
  .option('-A, --app <app>', 'Target app (from config/environments.yaml)')
  .option('-E, --env <env>', 'Target environment (ci/uat/prod)')
  .action((opts) => {
    const app = opts.app;
    const env = opts.env;
    const flowsDir = computeFlowsDir(app, env);
    const runner = new FlowRunner(flowsDir);
    const files = runner.listFlows();

    if (files.length === 0) {
      console.log('\n📂 Chưa có flow nào. Dùng `frt-test record` để tạo flow đầu tiên.\n');
      return;
    }

    const envLabel = env ? ` @ ${env}` : '';
    const label = app ? ` [app: ${app}${envLabel}]` : '';
    console.log(`\n📂 Flows${label} (${files.length} files):\n`);

    for (const file of files) {
      try {
        const flow = runner.loadFlow(file);
        const tags = flow.tags?.join(', ') || '-';
        const steps = flow.steps.length;
        const relPath = path.relative(FLOWS_DIR, file);
        console.log(`  📄 ${relPath}`);
        console.log(`     Name: ${flow.name} | Steps: ${steps} | Tags: ${tags}`);
        if (flow.description) console.log(`     Desc: ${flow.description}`);
        console.log('');
      } catch (err: any) {
        console.log(`  ⚠️  ${path.relative(FLOWS_DIR, file)} — Error: ${err.message}`);
      }
    }
  });

// ============================================================
// SCAFFOLD-CHAIN + ADD-APP commands
// ============================================================
registerScaffoldCommands(program);

// ============================================================
// MIGRATE-CONFIG command
// ============================================================
import { migrateConfig } from './migrate';

program
  .command('migrate-config')
  .description('Migrate config từ schema v1 → v2 (multi-env)')
  .option('--write', 'Apply changes (default: dry-run)')
  .option('--envs <envs>', 'Environments to create', 'ci,uat,prod')
  .action((opts) => {
    migrateConfig({
      write: opts.write || false,
      dryRun: !opts.write,
      envs: opts.envs.split(',').map((e: string) => e.trim()),
    });
  });

// ============================================================
// ADD-ENV command
// ============================================================
import { addEnv } from './add-env';

program
  .command('add-env')
  .description('Set URL cho env (null → real) hoặc thêm env mới')
  .requiredOption('--app <app>', 'App name')
  .requiredOption('--env <env>', 'Environment name')
  .requiredOption('--url <url>', 'URL to set')
  .action((opts) => {
    addEnv({ app: opts.app, env: opts.env, url: opts.url });
  });

// ============================================================
// ENVS command
// ============================================================
import { listEnvs } from './envs';

program
  .command('envs')
  .description('Liệt kê environments và URL status per app')
  .action(() => {
    listEnvs();
  });

// ============================================================
// CLONE-FLOW command
// ============================================================
import { cloneFlow } from './clone-flow';

program
  .command('clone-flow <file>')
  .description('Clone YAML test giữa env folders')
  .requiredOption('--app <app>', 'App name')
  .requiredOption('--from <env>', 'Source environment')
  .requiredOption('--to <env>', 'Destination environment')
  .option('--overwrite', 'Overwrite if exists')
  .action((file, opts) => {
    cloneFlow({ app: opts.app, from: opts.from, to: opts.to, file, overwrite: opts.overwrite });
  });

// ============================================================
// Parse
// ============================================================
program.parse();
