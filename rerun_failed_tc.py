#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Re-run failed TCs and update report
"""
import subprocess
import sys
import os
import time
from pathlib import Path

# 9 TCs fail (based on user info)
FAILED_TCS = [
    "TC13", "TC14", "TC15", "TC16", "TC17", 
    "TC18", "TC19", "TC20", "TC21"
]

BASE_DIR = Path(r"c:\Users\khangld10\Documents\APP\AI-MACAI2\.kiro\Automation selenium java\Selenium-java-master")
TESTNG_TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Rerun Failed TCs" verbose="1">
    <listeners>
        <listener class-name="listeners.TestListener"/>
        <listener class-name="utils.DashboardReportGenerator"/>
    </listeners>
    <test name="Failed TCs Rerun">
        <classes>
{classes}
        </classes>
    </test>
</suite>
"""

def create_rerun_testng():
    """Create testng.xml for failed TCs only"""
    classes = []
    for tc in FAILED_TCS:
        classes.append(f'            <class name="testcases.uat.Mudsku.{tc}"/>')
    
    content = TESTNG_TEMPLATE.format(classes="\n".join(classes))
    
    testng_path = BASE_DIR / "testng-rerun-failed.xml"
    with open(testng_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"✅ Created: {testng_path}")
    return testng_path

def run_failed_tcs(testng_path):
    """Run failed TCs using Maven"""
    print()
    print("=" * 80)
    print("RUNNING 9 FAILED TCs")
    print("=" * 80)
    print()
    
    cmd = [
        "mvn", "clean", "test",
        f"-Dsurefire.suiteXmlFiles={testng_path}"
    ]
    
    print(f"Command: {' '.join(cmd)}")
    print()
    
    start_time = time.time()
    
    try:
        result = subprocess.run(
            cmd,
            cwd=str(BASE_DIR),
            capture_output=False,  # Show output in real-time
            text=True
        )
        
        elapsed = time.time() - start_time
        
        print()
        print("=" * 80)
        print(f"COMPLETED in {elapsed:.1f}s")
        print("=" * 80)
        
        if result.returncode == 0:
            print("✅ All 9 TCs PASSED!")
            return True
        else:
            print("❌ Some TCs still FAILED")
            return False
            
    except Exception as e:
        print(f"❌ ERROR: {e}")
        return False

def show_report_location():
    """Show where to find the report"""
    report_path = BASE_DIR / "test-output" / "ExtentReport.html"
    dashboard_path = BASE_DIR / "test-output" / "dashboard.html"
    
    print()
    print("=" * 80)
    print("REPORT LOCATIONS")
    print("=" * 80)
    print()
    print(f"📊 Extent Report: {report_path}")
    print(f"📈 Dashboard:     {dashboard_path}")
    print()
    print("Open in browser to view updated results!")
    print("=" * 80)

def main():
    print("=" * 80)
    print("RE-RUN FAILED TCs TOOL")
    print("=" * 80)
    print()
    print(f"Will re-run {len(FAILED_TCS)} failed TCs:")
    for i, tc in enumerate(FAILED_TCS, 1):
        print(f"  {i}. {tc}")
    print()
    
    # Create testng.xml
    testng_path = create_rerun_testng()
    
    # Run tests
    success = run_failed_tcs(testng_path)
    
    # Show report
    show_report_location()
    
    return 0 if success else 1

if __name__ == "__main__":
    sys.exit(main())
