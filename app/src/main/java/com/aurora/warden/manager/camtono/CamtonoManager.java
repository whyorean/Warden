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

package com.aurora.warden.manager.camtono;

import android.content.Context;

public abstract class CamtonoManager {

    private static volatile CamtonoManager INSTANCE;

    public static CamtonoManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CamtonoManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CamtonoManager() {
                        @Override
                        public CamtonoBase camtono() {
                            return new CamtonoRoot(context);
                        }
                    };
                }
            }
        }
        return INSTANCE;
    }

    public abstract CamtonoBase camtono();
}
