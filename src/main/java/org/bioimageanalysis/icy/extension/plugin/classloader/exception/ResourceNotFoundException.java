/*
 * Copyright (c) 2010-2023. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package org.bioimageanalysis.icy.extension.plugin.classloader.exception;

import org.bioimageanalysis.icy.extension.plugin.classloader.ResourceType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Kamran Zafar
 * @author Thomas Musset
 */
public class ResourceNotFoundException extends JclException {
    private String resourceName;
    private ResourceType resourceType;

    /**
     * Default constructor
     */
    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(final String message) {
        super(message);
    }

    public ResourceNotFoundException(final @NotNull String resource, final String message) {
        super(message);
        resourceName = resource;
        determineResourceType(resource);
    }

    public ResourceNotFoundException(final Throwable e, final String resource, final String message) {
        super(message, e);
        resourceName = resource;
        determineResourceType(resource);
    }

    private void determineResourceType(final @NotNull String resourceName) {
        if (resourceName.toLowerCase().endsWith("." + ResourceType.CLASS.name().toLowerCase()))
            resourceType = ResourceType.CLASS;
        else if (resourceName.toLowerCase().endsWith("." + ResourceType.PROPERTIES.name().toLowerCase()))
            resourceType = ResourceType.PROPERTIES;
        else if (resourceName.toLowerCase().endsWith("." + ResourceType.XML.name().toLowerCase()))
            resourceType = ResourceType.XML;
        else
            resourceType = ResourceType.UNKNOWN;
    }

    /**
     * @return {@link ResourceType}
     */
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(final String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * @return {@link ResourceType}
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(final ResourceType resourceType) {
        this.resourceType = resourceType;
    }
}
