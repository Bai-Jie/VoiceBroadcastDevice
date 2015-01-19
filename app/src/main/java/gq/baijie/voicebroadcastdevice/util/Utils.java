/*
 * Copyright 2014 OptimalOrange
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
// copy from https://github.com/OptimalOrange/CoolTechnologies/blob/master/app/src/main/java/com/optimalorange/cooltechnologies/util/Utils.java

package gq.baijie.voicebroadcastdevice.util;

public class Utils {

    private Utils() {
    }

    public static String formatDuration(int duration) {
        int hours = duration / (60 * 60);
        int minutes = (duration - hours * (60 * 60)) / 60;
        int seconds = duration - hours * (60 * 60) - minutes * 60;
        String minutesStr, secondsStr, hoursStr = String.valueOf(hours);
        if (minutes < 10) {
            minutesStr = "0" + minutes;
        } else {
            minutesStr = String.valueOf(minutes);
        }
        if (seconds < 10) {
            secondsStr = "0" + seconds;
        } else {
            secondsStr = String.valueOf(seconds);
        }
        if (hours == 0) {
            return minutesStr + ":" + secondsStr;
        } else {
            return hoursStr + ":" + minutesStr + ":" + secondsStr;
        }
    }
}
