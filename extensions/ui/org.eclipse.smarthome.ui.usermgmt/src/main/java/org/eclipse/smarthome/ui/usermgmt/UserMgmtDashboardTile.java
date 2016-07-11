/**
 * Copyright (c) 2015-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.ui.usermgmt;

import org.openhab.ui.dashboard.DashboardTile;

/**
 * A dashboard tile must be registered as a service in order to appear on the openHAB dashboard.
 * Note that it is currently not possible to provide a background image - this needs to be
 * available within the dashboard bundle itself at the moment.
 *
 * @author Stefan Hettich
 *
 */
public class UserMgmtDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "User Mangement UI";
    }

    @Override
    public String getUrl() {
        return "../usermgmt/app";
    }

    @Override
    public String getOverlay() {
        return "html5";
    }

    @Override
    public String getImageUrl() {
        return "img/usermgmt.png"; // TODO create img in dashboard project.
    }

}
