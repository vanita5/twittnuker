/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.graphic.like.palette;

import android.animation.ArgbEvaluator;
import android.graphics.Color;

public final class LikePalette implements Palette {

    private final ArgbEvaluator evaluator = new ArgbEvaluator();
    private final float[] hsv = new float[3];

    @Override
    public int getParticleColor(int count, int index, float progress) {
        final double degree = 360.0 / count * index;
        hsv[0] = (float) degree;
        hsv[1] = 0.4f;
        hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }

    @Override
    public int getCircleColor(float progress) {
        return (Integer) evaluator.evaluate(progress, 0xFFDE4689, 0xFFCD8FF5);
    }
}