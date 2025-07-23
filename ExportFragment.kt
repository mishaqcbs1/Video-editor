@AndroidEntryPoint
class ExportFragment : Fragment() {
    
    private var _binding: FragmentExportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ExportViewModel by viewModels()
    
    private val args: ExportFragmentArgs by navArgs()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExportBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        observeViewModel()
        
        checkPremiumStatus()
    }
    
    private fun setupUI() {
        binding.btnExport.setOnClickListener {
            val addWatermark = !binding.switchRemoveWatermark.isChecked
            viewModel.exportVideo(args.videoPath, args.quality, addWatermark)
        }
        
        binding.btnSaveToGallery.setOnClickListener {
            // Will be implemented after export
        }
    }
    
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.exportState.collect { state ->
                when (state) {
                    is ExportViewModel.ExportState.Processing -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.btnExport.isEnabled = false
                    }
                    is ExportViewModel.ExportState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnExport.isEnabled = true
                        binding.btnSaveToGallery.isEnabled = true
                        binding.tvStatus.text = "Export completed!"
                    }
                    is ExportViewModel.ExportState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnExport.isEnabled = true
                        binding.tvStatus.text = state.message
                    }
                    else -> {}
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.progress.collect { progress ->
                binding.progressBar.progress = progress
                binding.tvProgress.text = "$progress%"
            }
        }
    }
    
    private fun checkPremiumStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val isPremium = viewModel.billingManager.isPremiumPurchased()
            binding.switchRemoveWatermark.isChecked = isPremium
            binding.switchRemoveWatermark.isEnabled = !isPremium
            
            if (!isPremium) {
                showWatermarkInfoDialog()
            }
        }
    }
    
    private fun showWatermarkInfoDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Watermark")
            .setMessage("Upgrade to premium to remove watermark from your videos")
            .setPositiveButton("Upgrade") { _, _ ->
                viewModel.billingManager.launchBillingFlow(requireActivity())
            }
            .setNegativeButton("Later", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
