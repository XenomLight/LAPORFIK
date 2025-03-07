package com.example.applaporfik.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.applaporfik.databinding.FragmentPilihKategoriBinding

class PilihKategoriFragment : Fragment() {

    private var _binding: FragmentPilihKategoriBinding? = null
    private val binding get() = _binding!!

    private val formViewModel: FormViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPilihKategoriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol back di toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Handle tombol back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.btnFasilitas.setOnClickListener {
            formViewModel.setSelectedCategory("Fasilitas & Layanan")
            findNavController().popBackStack()
        }

        binding.btnAkademik.setOnClickListener {
            formViewModel.setSelectedCategory("Akademik")
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}