/*
 * Warden
 * Copyright (C) 2020, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.aurora.warden.events;

import lombok.Data;


@Data
public class Event {

    private SubType subType;
    private String stringExtra;
    private int intExtra;
    private int status;

    public Event() {
    }

    public Event(SubType subType, String stringExtra, int status) {
        this.subType = subType;
        this.stringExtra = stringExtra;
        this.status = status;
    }

    public Event(SubType subType, String stringExtra) {
        this.subType = subType;
        this.stringExtra = stringExtra;
    }

    public Event(SubType subType, int intExtra) {
        this.subType = subType;
        this.intExtra = intExtra;
    }

    public Event(SubType subType) {
        this.subType = subType;
    }

    public enum SubType {
        /*Scanner Events*/
        SCAN_INIT,
        SCAN_UPDATE,
        SCAN_COMPLETED,
        SCAN_FAILED,
        SCAN_CANCELED,
        SCAN_REPORT_CHANGED,

        /*Script Events*/
        SCRIPT_INIT,
        SCRIPT_COMPLETED,
        SCRIPT_CANCELED,
        SCRIPT_FAILED,

        /*Debloater Events*/
        DEBLOAT_INIT,
        DEBLOAT_COMPLETED,
        DEBLOAT_FAILED,

        /*Nuke Events*/
        NUKE_INIT,
        NUKE_COMPLETED,
        NUKE_FAILED,

        /*Package Manager Events*/
        PACKAGE_INSTALLED,
        PACKAGE_UNINSTALLED,
        PACKAGE_CHANGED,
        PACKAGE_CLEARED,
        PACKAGE_SUSPENDED,
        PACKAGE_UNSUSPENDED,

        /*Misc*/
        NAVIGATED,
    }
}
