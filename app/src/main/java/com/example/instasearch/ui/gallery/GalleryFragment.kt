package com.example.instasearch.ui.gallery

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.instasearch.R
import com.example.instasearch.data.UnsplashPhoto
import com.example.instasearch.databinding.FragmentGalleryBinding
import com.example.instasearch.ui.details.DetailsFragment
import dagger.hilt.android.AndroidEntryPoint

const val KEY_UNSPLASH_PHOTO = "unsplash_photo"

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery), UnsplashPhotoAdapter.OnItemClickListener {

    private val viewModel by viewModels<GalleryViewModel>()

    private lateinit var binding: FragmentGalleryBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGalleryBinding.bind(view)

        val adapter = UnsplashPhotoAdapter(this)

        binding.apply {
            galleryRecyclerview.setHasFixedSize(true)
            galleryRecyclerview.itemAnimator = null
            galleryRecyclerview.adapter = adapter.withLoadStateHeaderAndFooter(
                header = PhotoLoadStateAdapter { adapter.retry() },
                footer = PhotoLoadStateAdapter { adapter.retry() }
            )
            retryButton.setOnClickListener {
                adapter.retry()
            }
        }

        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                galleryProgressBar.isVisible = loadState.source.refresh is LoadState.Loading
                galleryRecyclerview.isVisible = loadState.source.refresh is LoadState.NotLoading
                retryButton.isVisible = loadState.source.refresh is LoadState.Error
                errorTv.isVisible = loadState.source.refresh is LoadState.Error

                if (loadState.source.refresh is LoadState.NotLoading &&
                        loadState.append.endOfPaginationReached &&
                        adapter.itemCount < 1) {
                    galleryRecyclerview.isVisible = false
                    noResultsTv.isVisible = true
                } else {
                    noResultsTv.isVisible = false
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(photo: UnsplashPhoto) {
        val detailsFragment = DetailsFragment()
        val args = Bundle().apply {
            putParcelable(KEY_UNSPLASH_PHOTO, photo)
        }
        detailsFragment.arguments = args

        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack("details")
            .replace(R.id.nav_host_fragment_main, detailsFragment)
            .commit()

//        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment(photo)
//        findNavController().navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_gallery, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    binding.galleryRecyclerview.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

}