// Copyright (c) Microsoft Corporation
// All rights reserved. 
//
// MIT License
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
// documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
// the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and 
// to permit persons to whom the Software is furnished to do so, subject to the following conditions:
// The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
// BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 

package com.microsoft.frameworklauncher.common.exit;

import com.microsoft.frameworklauncher.common.model.ExitType;
import com.microsoft.frameworklauncher.common.web.WebCommon;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.util.ConverterUtils;

import java.io.IOException;
import java.io.Serializable;

public class AMDiagnostics implements Serializable {
  private Integer applicationExitCode;
  // Static App ExitInfo
  private String applicationExitDescription;
  // Dynamic App ExitInfo
  private String applicationExitDiagnostics;
  private ExitType applicationExitType;
  // App CompletionPolicy TriggerInfo
  private String applicationExitTriggerMessage;
  private String applicationExitTriggerTaskRoleName;
  private Integer applicationExitTriggerTaskIndex;

  public Integer getApplicationExitCode() {
    return applicationExitCode;
  }

  public void setApplicationExitCode(Integer applicationExitCode) {
    this.applicationExitCode = applicationExitCode;
  }

  public String getApplicationExitDescription() {
    return applicationExitDescription;
  }

  public void setApplicationExitDescription(String applicationExitDescription) {
    this.applicationExitDescription = applicationExitDescription;
  }

  public String getApplicationExitDiagnostics() {
    return applicationExitDiagnostics;
  }

  public void setApplicationExitDiagnostics(String applicationExitDiagnostics) {
    this.applicationExitDiagnostics = applicationExitDiagnostics;
  }

  public ExitType getApplicationExitType() {
    return applicationExitType;
  }

  public void setApplicationExitType(ExitType applicationExitType) {
    this.applicationExitType = applicationExitType;
  }

  public String getApplicationExitTriggerMessage() {
    return applicationExitTriggerMessage;
  }

  public void setApplicationExitTriggerMessage(String applicationExitTriggerMessage) {
    this.applicationExitTriggerMessage = applicationExitTriggerMessage;
  }

  public String getApplicationExitTriggerTaskRoleName() {
    return applicationExitTriggerTaskRoleName;
  }

  public void setApplicationExitTriggerTaskRoleName(String applicationExitTriggerTaskRoleName) {
    this.applicationExitTriggerTaskRoleName = applicationExitTriggerTaskRoleName;
  }

  public Integer getApplicationExitTriggerTaskIndex() {
    return applicationExitTriggerTaskIndex;
  }

  public void setApplicationExitTriggerTaskIndex(Integer applicationExitTriggerTaskIndex) {
    this.applicationExitTriggerTaskIndex = applicationExitTriggerTaskIndex;
  }

  public static String generateAndSerialize(
      int applicationExitCode,
      String applicationExitDiagnostics,
      String applicationExitTriggerMessage,
      String applicationExitTriggerTaskRoleName,
      Integer applicationExitTriggerTaskIndex) {
    return WebCommon.toJson(generate(
        applicationExitCode,
        applicationExitDiagnostics,
        applicationExitTriggerMessage,
        applicationExitTriggerTaskRoleName,
        applicationExitTriggerTaskIndex));
  }

  public static AMDiagnostics generate(
      int applicationExitCode,
      String applicationExitDiagnostics,
      String applicationExitTriggerMessage,
      String applicationExitTriggerTaskRoleName,
      Integer applicationExitTriggerTaskIndex) {
    FrameworkExitInfo applicationExitInfo = FrameworkExitSpec.getExitInfo(applicationExitCode);
    AMDiagnostics amDiagnostics = new AMDiagnostics();

    amDiagnostics.setApplicationExitCode(applicationExitCode);
    amDiagnostics.setApplicationExitDescription(applicationExitInfo.getDescription());
    amDiagnostics.setApplicationExitDiagnostics(applicationExitDiagnostics);
    amDiagnostics.setApplicationExitType(applicationExitInfo.getType());
    amDiagnostics.setApplicationExitTriggerMessage(applicationExitTriggerMessage);
    amDiagnostics.setApplicationExitTriggerTaskRoleName(applicationExitTriggerTaskRoleName);
    amDiagnostics.setApplicationExitTriggerTaskIndex(applicationExitTriggerTaskIndex);

    return amDiagnostics;
  }

  public static AMDiagnostics deserialize(String amDiagnostics) throws IOException {
    return WebCommon.toObject(amDiagnostics, AMDiagnostics.class);
  }

  public static String retrieve(YarnClient yarnClient, String applicationId) throws Exception {
    String amDiagnostics = yarnClient
        .getApplicationReport(ConverterUtils.toApplicationId(applicationId))
        .getDiagnostics();
    if (equalEmpty(amDiagnostics)) {
      throw new Exception("Retrieved empty AMDiagnostics for " + applicationId);
    }
    return amDiagnostics;
  }

  public static Boolean equalEmpty(String amDiagnostics) {
    return (amDiagnostics == null ||
        amDiagnostics.trim().isEmpty() ||
        amDiagnostics.trim().toLowerCase().equals("null"));
  }
}
