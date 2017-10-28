/*  Copyright (C) 2015-2017 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, João Paulo Barraca

    This file is part of Gadgetbridge.

    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package smartlife.monitorwearables.devices;

import smartlife.monitorwearables.entities.AbstractActivitySample;


/**
 * Interface to retrieve samples from the database, and also create and add samples to the database.
 * There are multiple device specific implementations, this interface defines the generic access.
 *
 * Note that the provided samples must typically be considered read-only, because they are immediately
 * removed from the session before they are returned.
 *
 * @param <T> the device/provider specific sample type (must extend AbstractActivitySample)
 */
public interface SampleProvider<T extends AbstractActivitySample> {

    int normalizeType(int rawType);

    float normalizeIntensity(int rawIntensity);

}
