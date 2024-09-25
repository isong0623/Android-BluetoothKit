package com.dreaming.bluetooth.framework.utils;

import android.os.Build;

import java.util.Comparator;

public class Version {
    public enum API implements Comparator<API> {
        Unknown           (0 , ""                ),
        Android_1_0       (1 , ""                ),
        Android_1_1       (2 , "Petit Four"      ),
        Android_1_5       (3 , "Cupcake"         ),
        Android_1_6       (4 , "Donut"           ),
        Android_2_0       (5 , "Eclair"          ),
        Android_2_0_1     (6 , "Eclair"          ),
        Android_2_1       (7 , "Eclair"          ),
        Android_2_2       (8 , "Froyo"           ),
        Android_2_3       (9 , "Gingerbread"     ),
        Android_2_3_3     (10, "Gingerbread"     ),
        Android_3_0       (11, "Honeycomb"       ),
        Android_3_1       (12, "Honeycomb"       ),
        Android_3_2       (13, "Honeycomb"       ),
        Android_4_0       (14, "IceCreamSandwich"),
        Android_4_0_3     (15, "IceCreamSandwich"),
        Android_4_1       (16, "Jelly Bean"      ),
        Android_4_2       (17, "Jelly Bean"      ),
        Android_4_3       (18, "Jelly Bean"      ),
        Android_4_4       (19, "KitKat"          ),
        Android_4_4W      (20, "KitKat Wear"     ),
        Android_5_0       (21, "Lollipop"        ),
        Android_5_1       (22, "Lollipop"        ),
        Android_6_0       (23, "Marshmallow"     ),
        Android_7_0       (24, "Nougat"          ),
        Android_7_1_1     (25, "Nougat"          ),
        Android_8_0       (26, "Oreo"            ),
        Android_8_1       (27, "Oreo"            ),
        Android_9_0       (28, "Pie"             ),
        Android_10_0      (29, "Q"               ),
        Android_10_0_Plus (29, "R"               ),
        Android_11_0      (31, "31"              ),
        Android_12_0      (32, "32"              ),
        Android_13_0      (33, "33"              ),
        Android_14_0      (34, "34"              ),
        Android_15_0      (35, "35"              ),
        ;

        public final int level;
        public final String nick;
        private API(int level, String nick){
            this.level = level;
            this.nick = nick;
        }

        public static API api(){
            return values()[Build.VERSION.SDK_INT];
        }

        public static API parse(int level){
            return values()[level];
        }

        @Override
        public int compare(API o1, API o2) {
            return  -((o1.level < o2.level) ? -1 : ((o1.level == o2.level) ? 0 : 1));
        }

        @Override
        public String toString() {
            return super.toString()
                    .replaceFirst("_"," ")
                    .replaceAll("_",".");
        }
    }

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
