package com.dreaming.bluetooth.framework.search.response;

import com.dreaming.bluetooth.framework.search.SearchResult;

public interface SearchResponse {

    void onSearchStarted();

    void onDeviceFounded(SearchResult device);

    void onSearchStopped();

    void onSearchCanceled();
}
