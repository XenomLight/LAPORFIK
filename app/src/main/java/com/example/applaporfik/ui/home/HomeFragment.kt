package com.example.applaporfik.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.applaporfik.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Inisialisasi View
        val btnFormLapor = view.findViewById<LinearLayout>(R.id.btn_form_lapor)
        val btnHasilLapor = view.findViewById<LinearLayout>(R.id.btn_hasil_lapor)

        val boxPanduan1 = view.findViewById<LinearLayout>(R.id.box_panduan_1)
        val textPanduan1 = view.findViewById<TextView>(R.id.text_panduan_1)

        val boxPanduan2 = view.findViewById<LinearLayout>(R.id.box_panduan_2)
        val textPanduan2 = view.findViewById<TextView>(R.id.text_panduan_2)

        val boxPanduan3 = view.findViewById<LinearLayout>(R.id.box_panduan_3)
        val textPanduan3 = view.findViewById<TextView>(R.id.text_panduan_3)

        // Fungsi untuk  tampilan teks panduan
        fun toggleVisibility(textView: TextView, box: LinearLayout) {
            if (textView.visibility == View.VISIBLE) {
                textView.visibility = View.GONE
                box.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_closed)
            } else {
                textView.visibility = View.VISIBLE
                box.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_open)
            }
        }

        // Klik pada setiap kartu panduan
        boxPanduan1.setOnClickListener { toggleVisibility(textPanduan1, boxPanduan1) }
        boxPanduan2.setOnClickListener { toggleVisibility(textPanduan2, boxPanduan2) }
        boxPanduan3.setOnClickListener { toggleVisibility(textPanduan3, boxPanduan3) }

        // Navigasi ke halaman Form Lapor
        btnFormLapor.setOnClickListener {
            findNavController().navigate(R.id.formFragment)
        }

//        // Navigasi ke halaman Hasil Lapor
//        btnHasilLapor.setOnClickListener {
//            findNavController().navigate(R.id.hasilFragment)
//        }

        return view
    }
}
