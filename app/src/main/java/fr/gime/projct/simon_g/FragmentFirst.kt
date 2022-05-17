package fr.gime.projct.simon_g

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import fr.gime.projct.simon_g.databinding.FirstFragmentBinding

class FragmentFirst : Fragment() {
    private lateinit var _binding: FirstFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FirstFragmentBinding.inflate(inflater)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.StartGameBtn.setOnClickListener {
            findNavController().navigate(R.id.action_to_mainSimon)
        }
    }
}