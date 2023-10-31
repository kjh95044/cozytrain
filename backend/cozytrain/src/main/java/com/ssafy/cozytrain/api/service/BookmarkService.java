package com.ssafy.cozytrain.api.service;

import com.ssafy.cozytrain.api.dto.BookmarkDto;

import java.util.List;

public interface BookmarkService {
    boolean saveBookmark(BookmarkDto bookmarkDto, Long memberId);
    List<BookmarkDto> getBookmark(Long memberId);
}