package com.example.applaporfik.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.applaporfik.databinding.FragmentFormBinding
import com.example.applaporfik.R
import android.util.Log

import com.example.applaporfik.ui.form.FormViewModel

class FormFragment : Fragment() {

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    // Gunakan ViewModel
    private val formViewModel: FormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("FormFragment", "Fragment ditampilkan dengan sukses")

        // Button Form
        binding.btnForm.setOnClickListener {
            Log.d("FormFragment", "Tombol 'Pilih Kategori' diklik")
            findNavController().navigate(R.id.action_formFragment_to_pilihKategoriFragment)
        }

        // Kategori view
        formViewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            Log.d("FormFragment", "Kategori dipilih: $category")
            binding.tvSelectedCategory.text = "Kategori Dipilih: $category"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
