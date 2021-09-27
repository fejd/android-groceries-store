package com.hieuwu.groceriesstore.presentation.explore

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hieuwu.groceriesstore.R
import com.hieuwu.groceriesstore.databinding.FragmentExploreBinding
import com.hieuwu.groceriesstore.di.ProductRepo
import com.hieuwu.groceriesstore.domain.repository.CategoryRepository
import com.hieuwu.groceriesstore.domain.repository.ProductRepository
import com.hieuwu.groceriesstore.presentation.adapters.CategoryItemAdapter
import com.hieuwu.groceriesstore.presentation.adapters.GridListItemAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding

    @ProductRepo
    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate<FragmentExploreBinding>(
            inflater, R.layout.fragment_explore, container, false
        )
        val viewModelFactory =
            ExploreViewModelFactory(categoryRepository, productRepository)

        val viewModel = ViewModelProvider(this, viewModelFactory)
            .get(ExploreViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        val productListAdapter = GridListItemAdapter(
            GridListItemAdapter.OnClickListener(
                clickListener = {
                },
                addToCartListener = {
                },
            )
        )
        binding.productRecyclerview.adapter = productListAdapter

        viewModel.productList.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                Timber.d("Empty")
            } else {
                binding.animationLayout.visibility = View.GONE
                binding.productRecyclerview.visibility = View.VISIBLE
                productListAdapter.submitList(it)
                Timber.d("Has item")
            }
        })

        val searchEditText =
            binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.WHITE)

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //Query product by name
                viewModel.searchProductByName(query!!)
                productListAdapter.notifyDataSetChanged()

                binding.animationLayout.visibility = View.GONE
                binding.productRecyclerview.visibility = View.VISIBLE
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                Timber.d("Search: $query")
                //Query product by name
                viewModel.searchProductByName(query!!)
                productListAdapter.notifyDataSetChanged()
                return true
            }
        })

        binding.searchView.setOnQueryTextFocusChangeListener { _, _ ->
            Timber.d("Search focused")
            //Hide category list
            //Show search result list with empty list product
            binding.categoryRecyclerview.visibility = View.GONE
            binding.animationLayout.visibility = View.VISIBLE
        }

        val closeSearchButton =
            binding.searchView.findViewById<AppCompatImageView>(androidx.appcompat.R.id.search_close_btn)
        closeSearchButton.setOnClickListener {
            binding.searchView.onActionViewCollapsed()
            Timber.d("Close search focused")
            //Show category list
            //Clear search result
            //Hide search result
            binding.categoryRecyclerview.visibility = View.VISIBLE
            binding.animationLayout.visibility = View.GONE

        }

        viewModel.categories.observe(viewLifecycleOwner, {})

        binding.categoryRecyclerview.adapter =
            CategoryItemAdapter(CategoryItemAdapter.OnClickListener {
                val direction =
                    ExploreFragmentDirections.actionExploreFragmentToProductListFragment(
                        it.name!!,
                        it.id
                    )
                findNavController().navigate(direction)
            })




        return binding.root
    }

}