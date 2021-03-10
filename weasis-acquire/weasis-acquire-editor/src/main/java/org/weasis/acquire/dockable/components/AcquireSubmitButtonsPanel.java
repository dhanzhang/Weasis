/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.acquire.dockable.components;

import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.weasis.acquire.Messages;
import org.weasis.acquire.dockable.components.actions.AcquireAction;
import org.weasis.acquire.dockable.components.actions.AcquireAction.Cmd;

public class AcquireSubmitButtonsPanel extends JPanel {
  private static final long serialVersionUID = 4890844708371941062L;

  private final AcquireActionButton cancelBtn;
  private final AcquireActionButton resetBtn;

  public AcquireSubmitButtonsPanel() {
    setBorder(UIManager.getBorder("TitledBorder.border"));
    FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 10, 10);
    setLayout(flowLayout);

    cancelBtn =
        new AcquireActionButton(Messages.getString("AcquireSubmitButtonsPanel.cancel"), Cmd.CANCEL);
    cancelBtn.setToolTipText(Messages.getString("AcquireSubmitButtonsPanel.return_prev"));
    resetBtn =
        new AcquireActionButton(Messages.getString("AcquireSubmitButtonsPanel.reset"), Cmd.RESET);
    resetBtn.setToolTipText(Messages.getString("AcquireSubmitButtonsPanel.reset_def"));

    add(cancelBtn);
    add(resetBtn);
  }

  public void setAcquireAction(AcquireAction acquireAction) {
    cancelBtn.setAcquireAction(acquireAction);
    resetBtn.setAcquireAction(acquireAction);
  }
}
