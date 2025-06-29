package com.example.applaporfik.fragment.form

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.applaporfik.adapter.ImagePreviewAdapter
import com.example.applaporfik.data.api.ApiService
import com.example.applaporfik.data.api.SubmitReportRequest
import com.example.applaporfik.databinding.FragmentFormBinding
import com.example.applaporfik.util.NetworkUtils
import com.example.applaporfik.util.SessionManager
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

class FormFragment : Fragment() {

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    private lateinit var apiService: ApiService
    private lateinit var imageAdapter: ImagePreviewAdapter
    
    private var selectedCategory: String? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (imageAdapter.getImages().size < 3) {
                    imageAdapter.addImage(uri)
                    updateImageCount()
                    showImagePreview()
                } else {
                    Toast.makeText(context, "Maximum 3 images allowed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sessionManager = SessionManager(requireContext())
        apiService = ApiService.create()
        
        setupImageAdapter()
        setupCategoryChips()
        setupFormButtons()

    }
    private fun setupImageAdapter() {
        imageAdapter = ImagePreviewAdapter(
            isEditable = true,
            onRemoveImage = { position ->
                imageAdapter.removeImage(position)
                updateImageCount()
                if (imageAdapter.getImages().isEmpty()) {
                    hideImagePreview()
                }
            }
        )

        binding.rvImagePreview.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = imageAdapter
        }
    }


    private fun setupCategoryChips() {
        binding.chipFacility.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipAcademic.isChecked = false
                selectedCategory = "facility"
                updateSelectedCategory()
                showForm()
            }
        }
        
        binding.chipAcademic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.chipFacility.isChecked = false
                selectedCategory = "academic"
                updateSelectedCategory()
                showForm()
            }
        }
    }

    private fun setupFormButtons() {
        binding.btnAddImage.setOnClickListener {
            openImagePicker()
        }
        
        binding.btnSend.setOnClickListener {
            submitReport()
        }
    }

    private fun updateSelectedCategory() {
        binding.tvSelectedCategory.text = "Category: ${selectedCategory?.capitalize()}"
    }

    private fun showForm() {
        binding.layoutForm.visibility = View.VISIBLE
    }

    private fun hideForm() {
        binding.layoutForm.visibility = View.GONE
    }

    private fun showImagePreview() {
        binding.rvImagePreview.visibility = View.VISIBLE
    }

    private fun hideImagePreview() {
        binding.rvImagePreview.visibility = View.GONE
    }

    private fun updateImageCount() {
        binding.tvImageCount.text = "${imageAdapter.getImages().size}/3 images"
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun submitReport() {
        // Validate inputs
        val title = binding.etTitle.text.toString().trim()
        val details = binding.etDetails.text.toString().trim()
        
        if (selectedCategory == null) {
            Toast.makeText(context, "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (title.isEmpty()) {
            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (details.isEmpty()) {
            Toast.makeText(context, "Please enter details", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!NetworkUtils.isNetworkAvailable(requireContext())) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                    }
                    return@launch
                }
                
                val sessionInfo = sessionManager.getSessionInfo()
                if (sessionInfo == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                    }
                    return@launch
                }
                
                // Submit report
                val reportRequest = SubmitReportRequest(
                    kategori = selectedCategory!!,
                    judul = title,
                    rincian = details
                )
                
                val reportResponse = apiService.submitReport(
                    token = "Bearer ${sessionInfo.token}",
                    request = reportRequest
                )
                
                if (reportResponse.success && reportResponse.report_id != null) {
                    // Upload images if any
                    val images = imageAdapter.getImages()
                    if (images.isNotEmpty()) {
                        uploadImages(reportResponse.report_id!!, images, sessionInfo.token)
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Report submitted successfully!", Toast.LENGTH_SHORT).show()
                            resetForm()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, reportResponse.message ?: "Failed to submit report", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.btnSend.isEnabled = true
                    }
                }
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnSend.isEnabled = true
                }
            }
        }
    }

    private suspend fun uploadImages(reportId: Int, images: List<Uri>, token: String) {
        try {
            val imageParts = mutableListOf<MultipartBody.Part>()
            
            for (i in images.indices) {
                val uri = images[i]
                val file = createTempFileFromUri(uri, "image_$i")
                
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("images", file.name, requestBody)
                imageParts.add(part)
            }
            
            val reportIdBody = okhttp3.RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                reportId.toString()
            )
            
            val uploadResponse = apiService.uploadImages(
                token = "Bearer $token",
                reportId = reportIdBody,
                images = imageParts
            )
            
            withContext(Dispatchers.Main) {
                if (uploadResponse.success) {
                    Toast.makeText(context, "Report submitted successfully!", Toast.LENGTH_SHORT).show()
                    resetForm()
                } else {
                    Toast.makeText(context, uploadResponse.message ?: "Failed to upload images", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
                binding.btnSend.isEnabled = true
            }
            
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error uploading images: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.btnSend.isEnabled = true
            }
        }
    }

    private fun createTempFileFromUri(uri: Uri, fileName: String): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().cacheDir, "$fileName.jpg")
        val outputStream = FileOutputStream(file)
        
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        return file
    }

    private fun resetForm() {
        binding.etTitle.text?.clear()
        binding.etDetails.text?.clear()
        binding.chipFacility.isChecked = false
        binding.chipAcademic.isChecked = false
        selectedCategory = null
        imageAdapter.clearImages()
        updateSelectedCategory()
        updateImageCount()
        hideForm()
        hideImagePreview()
        binding.progressBar.visibility = View.GONE
        binding.btnSend.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 