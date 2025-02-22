// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.profile.codeInspection.ui;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.daemon.impl.SeverityRegistrar;
import com.intellij.codeInsight.daemon.impl.SeverityUtil;
import com.intellij.codeInspection.InspectionsBundle;
import com.intellij.codeInspection.ex.SeverityEditorDialog;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Batkovich
 */
public abstract class LevelChooserAction extends ComboBoxAction implements DumbAware {

  private final SeverityRegistrar mySeverityRegistrar;
  private final boolean myIncludeDoNotShow;
  private HighlightSeverity myChosen = null;

  public LevelChooserAction(final SeverityRegistrar severityRegistrar) {
    this(severityRegistrar, false);
  }

  public LevelChooserAction(final SeverityRegistrar severityRegistrar, boolean includeDoNotShow) {
    mySeverityRegistrar = severityRegistrar;
    myIncludeDoNotShow = includeDoNotShow;
  }

  @NotNull
  @Override
  public DefaultActionGroup createPopupActionGroup(final JComponent anchor) {
    final DefaultActionGroup group = new DefaultActionGroup();
    for (final HighlightSeverity severity : getSeverities(mySeverityRegistrar, myIncludeDoNotShow)) {
      final HighlightSeverityAction action = new HighlightSeverityAction(severity);
      if (myChosen == null) {
        setChosen(action.getSeverity());
      }
      group.add(action);
    }
    group.addSeparator();
    group.add(new DumbAwareAction(InspectionsBundle.message("inspection.edit.severities.action")) {
      @Override
      public void actionPerformed(@NotNull final AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
          SeverityEditorDialog.show(project, myChosen, mySeverityRegistrar, true, severity -> {
            setChosen(severity);
            onChosen(severity);
          });
        }
      }
    });
    return group;
  }

  @NotNull
  public static List<@NotNull HighlightSeverity> getSeverities(final SeverityRegistrar severityRegistrar) {
    return getSeverities(severityRegistrar, true);
  }

  @NotNull
  public static List<@NotNull HighlightSeverity> getSeverities(final SeverityRegistrar severityRegistrar, boolean includeDoNotShow) {
    final List<HighlightSeverity> severities = new ArrayList<>();
    for (final SeverityRegistrar.SeverityBasedTextAttributes type : SeverityUtil.getRegisteredHighlightingInfoTypes(severityRegistrar)) {
      if (type.getType().isApplicableToInspections()) {
        severities.add(type.getSeverity());
      }
    }
    if (includeDoNotShow) {
      severities.add(HighlightSeverity.INFORMATION);
    }
    return severities;
  }

  protected abstract void onChosen(@NotNull HighlightSeverity severity);

  public void setChosen(@NotNull HighlightSeverity severity) {
    myChosen = severity;
    final Presentation templatePresentation = getTemplatePresentation();
    templatePresentation.setText(SingleInspectionProfilePanel.renderSeverity(severity));
    templatePresentation.setIcon(HighlightDisplayLevel.find(severity).getIcon());
  }

  private final class HighlightSeverityAction extends DumbAwareAction {
    @NotNull
    private final HighlightSeverity mySeverity;

    @NotNull
    HighlightSeverity getSeverity() {
      return mySeverity;
    }

    private HighlightSeverityAction(@NotNull HighlightSeverity severity) {
      mySeverity = severity;
      final Presentation presentation = getTemplatePresentation();
      presentation.setText(SingleInspectionProfilePanel.renderSeverity(severity));
      presentation.setIcon(HighlightDisplayLevel.find(severity).getIcon());
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
      final HighlightSeverity severity = getSeverity();
      setChosen(severity);
      onChosen(severity);
    }
  }
}
