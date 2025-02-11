/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.maven.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MavenWorkspaceSettings {
  /**
   * @deprecated Do not use this public field
   */
  @Deprecated
  public MavenGeneralSettings generalSettings = new MavenGeneralSettings();

  /**
   * @deprecated Do not use this public field
   */
  @Deprecated(forRemoval = true)
  public MavenImportingSettings importingSettings = new MavenImportingSettings();

  public List<String> enabledProfiles = new ArrayList<>();
  public List<String> disabledProfiles = new ArrayList<>();

  public void setEnabledProfiles(Collection<String> profiles) {
    enabledProfiles.clear();
    enabledProfiles.addAll(profiles);
  }

  public void setDisabledProfiles(Collection<String> profiles) {
    disabledProfiles.clear();
    disabledProfiles.addAll(profiles);
  }

  public MavenGeneralSettings getGeneralSettings() {
    return generalSettings;
  }

  public void setGeneralSettings(MavenGeneralSettings generalSettings) {
    this.generalSettings = generalSettings;
  }

  public MavenImportingSettings getImportingSettings() {
    return importingSettings;
  }

  public void setImportingSettings(MavenImportingSettings importingSettings) {
    this.importingSettings = importingSettings;
  }
}