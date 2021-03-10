/*
 * Copyright (c) 2009-2020 Weasis Team and other contributors.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0, or the Apache
 * License, Version 2.0 which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package org.weasis.core.ui.editor.image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import org.weasis.core.api.gui.util.ActionState;
import org.weasis.core.api.gui.util.ActionW;
import org.weasis.core.api.gui.util.SliderChangeListener;
import org.weasis.core.api.gui.util.ToggleButtonListener;
import org.weasis.core.ui.Messages;
import org.weasis.core.ui.util.WtoolBar;

@SuppressWarnings("serial")
public class RotationToolBar extends WtoolBar {

  public RotationToolBar(final ImageViewerEventManager<?> eventManager, int index) {
    super(Messages.getString("RotationToolBar.rotationBar"), index);
    if (eventManager == null) {
      throw new IllegalArgumentException("EventManager cannot be null");
    }

    final JButton jButtonRotate90 =
        new JButton(new ImageIcon(MouseActions.class.getResource("/icon/32x32/rotate.png")));
    jButtonRotate90.setToolTipText(Messages.getString("RotationToolBar.90"));
    jButtonRotate90.addActionListener(
        e -> {
          ActionState rotateAction = eventManager.getAction(ActionW.ROTATION);
          if (rotateAction instanceof SliderChangeListener) {
            final SliderChangeListener rotation = (SliderChangeListener) rotateAction;
            rotation.setSliderValue((rotation.getSliderValue() + 90) % 360);
          }
        });
    ActionState rotateAction = eventManager.getAction(ActionW.ROTATION);
    if (rotateAction != null) {
      rotateAction.registerActionState(jButtonRotate90);
    }
    add(jButtonRotate90);

    final JToggleButton jButtonFlip =
        new JToggleButton(new ImageIcon(MouseActions.class.getResource("/icon/32x32/flip.png")));
    jButtonFlip.setToolTipText(Messages.getString("RotationToolBar.flip"));
    ActionState flipAction = eventManager.getAction(ActionW.FLIP);
    if (flipAction instanceof ToggleButtonListener) {
      ((ToggleButtonListener) flipAction).registerActionState(jButtonFlip);
    }
    add(jButtonFlip);
  }
}
