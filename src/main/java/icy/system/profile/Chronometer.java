/*
 * Copyright (c) 2010-2024. Institut Pasteur.
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

package icy.system.profile;

import icy.system.logging.IcyLogger;

/**
 * This class provide a simple chronometer using Calendar.getInstance().getTimeInMillis();
 *
 * @author Fabrice de Chaumont
 * @author Thomas Musset
 */
public class Chronometer {
    long startTimeInNs;
    String descriptionString;

    public Chronometer(final String descriptionString) {
        this.descriptionString = descriptionString;
        startTimeInNs = System.nanoTime();
        IcyLogger.info(Chronometer.class, descriptionString + ": chrono Started");
    }

    public long getNanos() {
        return System.nanoTime() - startTimeInNs;
    }

    public void displayMs() {
        IcyLogger.info(Chronometer.class, descriptionString + "(ms): " + getNanos() / 1000000f);
    }

    public void displayInSeconds() {
        IcyLogger.info(Chronometer.class, descriptionString + "(s): " + getNanos() / 1000000000f);
    }

    @Override
    public String toString() {
        return descriptionString + "(nanos): " + getNanos();
    }
}
