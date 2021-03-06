package com.woocommerce.android.ui.orders

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.analytics.AnalyticsTracker.Stat
import com.woocommerce.android.widgets.AlignedDividerDecoration
import kotlinx.android.synthetic.main.order_detail_product_list.view.*
import org.wordpress.android.fluxc.model.WCOrderModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.order.CoreOrderStatus

class OrderDetailProductListView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null)
    : ConstraintLayout(ctx, attrs) {
    init {
        View.inflate(context, R.layout.order_detail_product_list, this)
    }
    private lateinit var divider: AlignedDividerDecoration

    /**
     * Initialize and format this view.
     *
     * @param [order] The order containing the product list to display.
     * @param [expanded] If true, expanded view will be shown, else collapsed view.
     * @param [formatCurrencyForDisplay] Function to use for formatting currencies for display.
     * @param [listener] Listener for routing click actions. If null, the buttons will be hidden.
     */
    fun initView(
        order: WCOrderModel,
        expanded: Boolean,
        formatCurrencyForDisplay: (String?) -> String,
        listener: OrderActionListener? = null
    ) {
        divider = AlignedDividerDecoration(context,
                DividerItemDecoration.VERTICAL, R.id.productInfo_name, clipToMargin = true)

        ContextCompat.getDrawable(context, R.drawable.list_divider)?.let { drawable ->
            divider.setDrawable(drawable)
        }

        val viewManager = LinearLayoutManager(context)
        val viewAdapter = ProductListAdapter(order.getLineItemList(), formatCurrencyForDisplay, expanded)

        listener?.let {
            if (order.status == CoreOrderStatus.PROCESSING.value) {
                productList_btnFulfill.visibility = View.VISIBLE
                productList_btnDetails.visibility = View.GONE
                productList_btnDetails.setOnClickListener(null)
                productList_btnFulfill.setOnClickListener {
                    AnalyticsTracker.track(Stat.ORDER_DETAIL_FULFILL_ORDER_BUTTON_TAPPED)
                    listener.openOrderFulfillment(order)
                }
            } else {
                productList_btnFulfill.visibility = View.GONE
                productList_btnDetails.visibility = View.VISIBLE
                productList_btnDetails.setOnClickListener {
                    AnalyticsTracker.track(Stat.ORDER_DETAIL_PRODUCT_DETAIL_BUTTON_TAPPED)
                    listener.openOrderProductList(order)
                }
                productList_btnFulfill.setOnClickListener(null)
            }
        } ?: hideButtons()

        productList_products.apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            itemAnimator = DefaultItemAnimator()
            adapter = viewAdapter
        }

        if (expanded) {
            productList_products.addItemDecoration(divider)
        }
    }

    fun updateView(order: WCOrderModel, expanded: Boolean, listener: OrderActionListener? = null) {
        listener?.let {
            if (order.status == CoreOrderStatus.PROCESSING.value) {
                productList_btnFulfill.visibility = View.VISIBLE
                productList_btnDetails.visibility = View.GONE
                productList_btnDetails.setOnClickListener(null)
                productList_btnFulfill.setOnClickListener {
                    listener.openOrderFulfillment(order)
                }
            } else {
                productList_btnFulfill.visibility = View.GONE
                productList_btnDetails.visibility = View.VISIBLE
                productList_btnDetails.setOnClickListener {
                    listener.openOrderProductList(order)
                }
                productList_btnFulfill.setOnClickListener(null)
            }
        } ?: hideButtons()

        if (expanded) {
            productList_products.addItemDecoration(divider)
        }
    }

    private fun hideButtons() {
        productList_btnFulfill.setOnClickListener(null)
        productList_btnDetails.setOnClickListener(null)
        productList_btnFulfill.visibility = View.GONE
        productList_btnDetails.visibility = View.GONE
    }

    class ProductListAdapter(
        private val orderItems: List<WCOrderModel.LineItem>,
        private val formatCurrencyForDisplay: (String?) -> String,
        private var isExpanded: Boolean
    ) : RecyclerView.Adapter<ProductListAdapter.ViewHolder>() {
        class ViewHolder(val view: OrderDetailProductItemView) : RecyclerView.ViewHolder(view)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view: OrderDetailProductItemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.order_detail_product_list_item, parent, false)
                    as OrderDetailProductItemView
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.view.initView(orderItems[position], isExpanded, formatCurrencyForDisplay)
        }

        override fun getItemCount() = orderItems.size
    }
}
