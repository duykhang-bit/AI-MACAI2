/**
 * FRT Test Recorder — Converter
 * Convert Playwright codegen output (TypeScript) → YAML flow format
 * Dùng khi tester record bằng `npx playwright codegen` rồi muốn convert sang YAML
 */

import * as fs from 'fs';
import * as path from 'path';
import * as yaml from 'js-yaml';
import { FlowFile, FlowStep } from './types';

export class FlowConverter {
  /**
   * Convert Playwright TypeScript code to YAML flow
   */
  convertFromPlaywright(tsCode: string, options: {
    flowName: string;
    description?: string;
    tags?: string[];
    author?: string;
  }): FlowFile {
    const steps = this.parsePlaywrightCode(tsCode);

    return {
      name: options.flowName,
      description: options.description || `Converted from Playwright codegen`,
      tags: options.tags || ['converted'],
      author: options.author || 'converter',
      created: new Date().toISOString().split('T')[0],
      config: {
        baseUrl: this.extractBaseUrl(tsCode) || process.env.BASE_URL || 'http://localhost:3000',
        timeout: 30000,
      },
      variables: this.extractVariables(steps),
      steps,
      assertions: [],
    };
  }

  /**
   * Parse Playwright TS code into FlowSteps
   */
  private parsePlaywrightCode(code: string): FlowStep[] {
    const steps: FlowStep[] = [];
    const lines = code.split('\n');

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('import')
        || trimmed.startsWith('test') || trimmed === '{' || trimmed === '}') {
        continue;
      }

      const step = this.parseLine(trimmed);
      if (step) steps.push(step);
    }

    return steps;
  }

  /**
   * Parse a single line of Playwright code into a FlowStep
   */
  private parseLine(line: string): FlowStep | null {
    // page.goto('url')
    const gotoMatch = line.match(/page\.goto\(['"](.+?)['"]/);
    if (gotoMatch) {
      return {
        name: `Navigate to ${gotoMatch[1]}`,
        action: 'goto',
        value: gotoMatch[1],
        waitAfter: 'networkIdle',
      };
    }

    // page.locator('selector').click()
    const clickLocatorMatch = line.match(/page\.locator\(['"](.+?)['"]\)\.click/);
    if (clickLocatorMatch) {
      return {
        name: `Click ${clickLocatorMatch[1]}`,
        action: 'click',
        selector: clickLocatorMatch[1],
      };
    }

    // page.getByRole('role', { name: 'name' }).click()
    const clickRoleMatch = line.match(/page\.getByRole\(['"](.+?)['"],\s*\{\s*name:\s*['"](.+?)['"]/);
    if (clickRoleMatch && line.includes('.click')) {
      return {
        name: `Click ${clickRoleMatch[2]}`,
        action: 'click',
        role: { type: clickRoleMatch[1], name: clickRoleMatch[2] },
      };
    }

    // page.getByText('text').click()
    const clickTextMatch = line.match(/page\.getByText\(['"](.+?)['"]\)\.click/);
    if (clickTextMatch) {
      return {
        name: `Click "${clickTextMatch[1]}"`,
        action: 'click',
        text: clickTextMatch[1],
      };
    }

    // page.getByPlaceholder('placeholder').click()
    const clickPlaceholderMatch = line.match(/page\.getByPlaceholder\(['"](.+?)['"]\)\.click/);
    if (clickPlaceholderMatch) {
      return {
        name: `Click placeholder "${clickPlaceholderMatch[1]}"`,
        action: 'click',
        placeholder: clickPlaceholderMatch[1],
      };
    }

    // page.getByTestId('testId').click()
    const clickTestIdMatch = line.match(/page\.getByTestId\(['"](.+?)['"]\)\.click/);
    if (clickTestIdMatch) {
      return {
        name: `Click testId "${clickTestIdMatch[1]}"`,
        action: 'click',
        testId: clickTestIdMatch[1],
      };
    }

    // page.getByRole('role', { name: 'name' }).fill('value')
    const fillRoleMatch = line.match(/page\.getByRole\(['"](.+?)['"],\s*\{\s*name:\s*['"](.+?)['"]\s*\}\)\.fill\(['"](.+?)['"]\)/);
    if (fillRoleMatch) {
      return {
        name: `Fill ${fillRoleMatch[2]} with "${fillRoleMatch[3]}"`,
        action: 'fill',
        role: { type: fillRoleMatch[1], name: fillRoleMatch[2] },
        value: fillRoleMatch[3],
      };
    }

    // page.locator('selector').fill('value')
    const fillMatch = line.match(/page\.locator\(['"](.+?)['"]\)\.fill\(['"](.+?)['"]\)/);
    if (fillMatch) {
      return {
        name: `Fill ${fillMatch[1]} with "${fillMatch[2]}"`,
        action: 'fill',
        selector: fillMatch[1],
        value: fillMatch[2],
      };
    }

    // page.getByLabel('label').fill('value')
    const fillLabelMatch = line.match(/page\.getByLabel\(['"](.+?)['"]\)\.fill\(['"](.+?)['"]\)/);
    if (fillLabelMatch) {
      return {
        name: `Fill ${fillLabelMatch[1]} with "${fillLabelMatch[2]}"`,
        action: 'fill',
        label: fillLabelMatch[1],
        value: fillLabelMatch[2],
      };
    }

    // page.getByPlaceholder('placeholder').fill('value')
    const fillPlaceholderMatch = line.match(/page\.getByPlaceholder\(['"](.+?)['"]\)\.fill\(['"](.+?)['"]\)/);
    if (fillPlaceholderMatch) {
      return {
        name: `Fill placeholder "${fillPlaceholderMatch[1]}" with "${fillPlaceholderMatch[2]}"`,
        action: 'fill',
        placeholder: fillPlaceholderMatch[1],
        value: fillPlaceholderMatch[2],
      };
    }

    // page.getByTestId('testId').fill('value')
    const fillTestIdMatch = line.match(/page\.getByTestId\(['"](.+?)['"]\)\.fill\(['"](.+?)['"]\)/);
    if (fillTestIdMatch) {
      return {
        name: `Fill testId "${fillTestIdMatch[1]}" with "${fillTestIdMatch[2]}"`,
        action: 'fill',
        testId: fillTestIdMatch[1],
        value: fillTestIdMatch[2],
      };
    }

    // page.keyboard.press('key')
    const keyMatch = line.match(/page\.keyboard\.press\(['"](.+?)['"]\)/);
    if (keyMatch) {
      return {
        name: `Press ${keyMatch[1]}`,
        action: 'keyboard',
        key: keyMatch[1],
      };
    }

    // page.waitForTimeout(ms)
    const waitMatch = line.match(/page\.waitForTimeout\((\d+)\)/);
    if (waitMatch) {
      return {
        name: `Wait ${waitMatch[1]}ms`,
        action: 'wait',
        delay: parseInt(waitMatch[1]),
      };
    }

    // page.waitForLoadState('state')
    const loadStateMatch = line.match(/page\.waitForLoadState\(['"](.+?)['"]\)/);
    if (loadStateMatch) {
      // Skip — will be handled as waitAfter on previous step
      return null;
    }

    // page.waitForURL('pattern')
    const waitUrlMatch = line.match(/page\.waitForURL\(['"](.+?)['"]/);
    if (waitUrlMatch) {
      return {
        name: `Wait for URL: ${waitUrlMatch[1]}`,
        action: 'wait',
        value: waitUrlMatch[1],
        timeout: 30000,
      };
    }

    // page.screenshot
    const screenshotMatch = line.match(/page\.screenshot\(\{.*path:\s*['"](.+?)['"]/);
    if (screenshotMatch) {
      return {
        name: `Screenshot: ${screenshotMatch[1]}`,
        action: 'screenshot',
        value: screenshotMatch[1],
      };
    }

    return null;
  }

  /**
   * Extract base URL from code
   */
  private extractBaseUrl(code: string): string | null {
    const match = code.match(/page\.goto\(['"](.+?)['"]/);
    if (match) {
      try {
        const url = new URL(match[1]);
        return `${url.protocol}//${url.host}`;
      } catch { return null; }
    }
    return null;
  }

  /**
   * Extract potential variables from steps (repeated values)
   */
  private extractVariables(steps: FlowStep[]): Record<string, string> {
    const vars: Record<string, string> = {};
    const valueCounts: Record<string, number> = {};

    // Count value occurrences
    for (const step of steps) {
      if (step.value && step.action === 'fill') {
        valueCounts[step.value] = (valueCounts[step.value] || 0) + 1;
      }
    }

    // Values used more than once become variables
    let varIndex = 1;
    for (const [value, count] of Object.entries(valueCounts)) {
      if (count > 1) {
        const varName = `var_${varIndex++}`;
        vars[varName] = value;
      }
    }

    return vars;
  }

  /**
   * Convert a .spec.ts file to YAML and save
   */
  convertFile(inputPath: string, outputDir: string, options: {
    flowName?: string;
    tags?: string[];
    author?: string;
  } = {}): string {
    const code = fs.readFileSync(inputPath, 'utf8');
    const baseName = path.basename(inputPath, path.extname(inputPath));
    const flowName = options.flowName || baseName;

    const flow = this.convertFromPlaywright(code, {
      flowName,
      tags: options.tags,
      author: options.author,
    });

    if (!fs.existsSync(outputDir)) {
      fs.mkdirSync(outputDir, { recursive: true });
    }

    const outputPath = path.join(outputDir, `${baseName}.yaml`);
    const yamlContent = [
      `# Converted from: ${path.basename(inputPath)}`,
      `# Date: ${new Date().toISOString().split('T')[0]}`,
      `# ⚠️  Review and adjust selectors/waits after conversion`,
      '',
      yaml.dump(flow, { lineWidth: 120, noRefs: true }),
    ].join('\n');

    fs.writeFileSync(outputPath, yamlContent, 'utf8');
    return outputPath;
  }
}
