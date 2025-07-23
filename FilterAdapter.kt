class FilterAdapter(
    private val filters: Array<VideoFilter>,
    private val onFilterSelected: (VideoFilter) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return FilterViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(filters[position])
    }
    
    override fun getItemCount() = filters.size
    
    inner class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemFilterBinding.bind(itemView)
        
        fun bind(filter: VideoFilter) {
            binding.tvFilterName.text = filter.name
            binding.ivFilterPreview.setImageResource(filter.previewResId)
            
            itemView.setOnClickListener {
                onFilterSelected(filter)
            }
        }
    }
}
