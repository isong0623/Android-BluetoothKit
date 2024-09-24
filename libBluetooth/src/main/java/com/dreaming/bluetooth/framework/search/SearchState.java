package com.dreaming.bluetooth.framework.search;

public enum SearchState {
    Unknown (0),
    Start   (1),
    Stop    (2),
    Cancel  (3),
    Finished(4),
    ;

    public final int state;
    private SearchState(int state){
        this.state = state;
    }

    public static SearchState parse(int state){
        switch (state){
            case 1: return Start   ;
            case 2: return Stop    ;
            case 3: return Cancel  ;
            case 4: return Finished;
        }
        return Unknown;
    }
}
