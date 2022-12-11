package com.example.fragment_navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.fragment_navigation.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)

        val button_cold: Button = view.findViewById(R.id.button_cold);
        button_cold.setOnClickListener{
            Toast.makeText(context, "cold", Toast.LENGTH_SHORT).show()
        }

        val button_hot: Button = view.findViewById(R.id.button_hot);
        button_hot.setOnClickListener{
            Toast.makeText(context, "hot", Toast.LENGTH_SHORT).show()
        }

        val button_auto: Button = view.findViewById(R.id.button_auto);
        button_auto.setOnClickListener{
            Toast.makeText(context, "auto", Toast.LENGTH_SHORT).show()
        }
        return view
    }
}