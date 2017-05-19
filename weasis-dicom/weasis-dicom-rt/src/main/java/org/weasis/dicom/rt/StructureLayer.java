/*******************************************************************************
 * Copyright (c) 2017 Weasis Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.dicom.rt;

import java.util.Objects;

import org.weasis.core.ui.model.layer.LayerType;
import org.weasis.core.ui.model.layer.imp.DefaultLayer;

public class StructureLayer {
    private final Structure structure;
    private final DefaultLayer layer;

    public StructureLayer(Structure structure) {
        this.structure = Objects.requireNonNull(structure);
        this.layer = new DefaultLayer(LayerType.DICOM_RT);
        this.layer.setName(structure.getRoiName());
    }

    public Structure getStructure() {
        return structure;
    }

    public boolean isSelected() {
        return layer.getVisible();
    }

    public DefaultLayer getLayer() {
        return layer;
    }

    @Override
    public String toString() {
        return structure.toString();
    }
}
