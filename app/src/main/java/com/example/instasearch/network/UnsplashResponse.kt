package com.example.instasearch.network

import com.example.instasearch.data.UnsplashPhoto

data class UnsplashResponse(
    val results: List<UnsplashPhoto>
) {
}