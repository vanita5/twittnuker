/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import java.io.Serializable;
import java.util.Map;


public interface MediaEntity extends UrlEntity {

	long getId();


    Map<String, Feature> getFeatures();

    String getMediaUrl();


    String getMediaUrlHttps();


    Map<String, Size> getSizes();


    Type getType();

    enum Type {
        PHOTO, VIDEO, ANIMATED_GIF, UNKNOWN;

        public static Type parse(String typeString) {
            if ("photo".equalsIgnoreCase(typeString)) {
                return PHOTO;
            } else if ("video".equalsIgnoreCase(typeString)) {
                return VIDEO;
            } else if ("animated_gif".equalsIgnoreCase(typeString)) {
                return ANIMATED_GIF;
            }
            return UNKNOWN;
        }
    }

    VideoInfo getVideoInfo();

    interface VideoInfo {

        Variant[] getVariants();

        long[] getAspectRatio();

        long getDuration();

        interface Variant {

            String getContentType();

            String getUrl();

            long getBitrate();
        }

    }

    interface Size {
        String THUMB = "thumb";
        String SMALL = "small";
        String MEDIUM = "medium";
        String LARGE = "large";
		int FIT = 100;
		int CROP = 101;

		int getHeight();

        String getResize();

        int getWidth();
    }

    /**
     * Created by mariotaku on 15/3/31.
     */
    interface Feature {

        interface Face {

            int getX();

            int getY();

            int getHeight();

			int getWidth();
		}
    }
}