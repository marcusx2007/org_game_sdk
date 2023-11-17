package com.gaming.core.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.gaming.core.pri.ConstPool
import com.gaming.core.extensions.mipmapResource

class GamingMenu @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    LinearLayout(context, attrs), OnTouchListener {
    private val THRESHOLD_DRAGGING = 2
    private val MENU_BUTTON_SIZE = dp2px(40f)


    private fun dp2px(dp: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private var mMenuState = ConstPool.STATE_DOCKED
    private var mMenuDockType = ConstPool.DOCK_LEFT
    private var mMenuLayout: View? = null
    private var mMenuButton: ImageView? = null
    private var mMenuPlaceholder: View? = null
    private var mMenuRefreshButton: View? = null
    private var mMenuCloseButton: View? = null
    private var mListener: OnMenuClickListener? = null
    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mTouchStartX = 0f
    private var mTouchStartY = 0f

    init {
        init(context)
        setOnTouchListener(this)
    }

    private fun init(context: Context) {

        mMenuLayout = LinearLayout(context).apply {
            this.orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(context.mipmapResource("icon_bg"))

            mMenuButton = ImageView(context).apply {
                setImageResource(context.mipmapResource("icon_shrinked"))
            }
            this.addView(mMenuButton, LayoutParams(dp2px(40f), dp2px(40f)))


            mMenuPlaceholder = View(context)
            this.addView(mMenuPlaceholder, LayoutParams(dp2px(10f), 0))

            mMenuRefreshButton = ImageView(context).apply {
                setImageResource(context.mipmapResource("icon_refresh"))
            }
            this.addView(mMenuRefreshButton, LayoutParams(dp2px(40f), dp2px(40f)))

            //closeBtn
            mMenuCloseButton = ImageView(context).apply {
                setImageResource(context.mipmapResource("icon_close"))
            }
            this.addView(mMenuCloseButton, LayoutParams(dp2px(40f), dp2px(40f)))

        }
        this.addView(mMenuLayout)
        mMenuRefreshButton?.setOnClickListener { _: View? ->
            mListener?.onRefresh()
        }
        mMenuCloseButton?.setOnClickListener { _: View? ->
            mListener?.onClose()
        }
        updateMenuByState()
    }

    fun setMenuListener(mListener: OnMenuClickListener?) {
        this.mListener = mListener
    }

    fun show() {
        val activity = context as Activity
        val contentLayout = activity.findViewById<FrameLayout>(android.R.id.content)
        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        contentLayout.addView(this, layoutParams)
        resetPositionDelayed()
    }

    private fun updateMenuByState() {
        when (mMenuState) {
            ConstPool.STATE_DOCKED, ConstPool.STATE_DRAGGING -> {
                scaleX = 1f
                mMenuLayout!!.setBackgroundResource(0)
                mMenuButton!!.setImageResource(context.mipmapResource("icon_shrinked"))
                mMenuPlaceholder!!.visibility = GONE
                mMenuRefreshButton!!.visibility = GONE
                mMenuCloseButton!!.visibility = GONE
            }

            ConstPool.STATE_EXPANDED -> {
                scaleX = if (ConstPool.DOCK_LEFT == mMenuDockType) 1f else (-1).toFloat()
                mMenuLayout!!.setBackgroundResource(context.mipmapResource("icon_bg"))
                mMenuButton!!.setImageResource(context.mipmapResource("icon_menu"))
                mMenuPlaceholder!!.visibility = INVISIBLE
                mMenuRefreshButton!!.visibility = VISIBLE
                mMenuCloseButton!!.visibility = VISIBLE
            }
        }
    }

    fun resetPositionDelayed() {
        post {
            initScreenSize()
            resetXByDockType()
            y = (mScreenHeight * 0.35f).toInt().toFloat()
        }
    }

    private val screenSize: IntArray
        get() {
            val width: Int
            val height: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowManager =
                    context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                val windowMetrics = windowManager.currentWindowMetrics
                width = windowMetrics.bounds.width()
                height = windowMetrics.bounds.height()
            } else {
                val dm = context.resources.displayMetrics
                width = dm.widthPixels
                height = dm.heightPixels
            }
            return intArrayOf(width, height)
        }


    private fun initScreenSize() {
        val screenSize = screenSize
        mScreenWidth = screenSize[0]
        mScreenHeight = screenSize[1]
    }

    private fun resetXByDockType() {
        when (mMenuDockType) {
            ConstPool.DOCK_LEFT -> x = 0f
            ConstPool.DOCK_RIGHT -> x = (mScreenWidth - width).toFloat()
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mTouchStartX = x
                mTouchStartY = y
                true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = x - mTouchStartX
                val deltaY = y - mTouchStartY
                if (Math.abs(deltaX) > THRESHOLD_DRAGGING
                    && Math.abs(deltaY) > THRESHOLD_DRAGGING
                ) {
                    mMenuState = ConstPool.STATE_DRAGGING
                    updateMenuByState()
                    setX(x - MENU_BUTTON_SIZE)
                    setY(y - MENU_BUTTON_SIZE)
                }
                true
            }

            MotionEvent.ACTION_UP -> {
                if (ConstPool.STATE_DOCKED == mMenuState) {
                    mMenuState = ConstPool.STATE_EXPANDED
                } else if (ConstPool.STATE_DRAGGING == mMenuState) {
                    mMenuState = ConstPool.STATE_DOCKED
                } else if (ConstPool.STATE_EXPANDED == mMenuState) {
                    mMenuState = ConstPool.STATE_DOCKED
                }
                updateMenuByState()
                mMenuDockType = if (x <= mScreenWidth / 2.0f) {
                    ConstPool.DOCK_LEFT
                } else {
                    ConstPool.DOCK_RIGHT
                }
                resetXByDockType()
                run {
                    mTouchStartY = 0f
                    mTouchStartX = mTouchStartY
                }
                true
            }

            MotionEvent.ACTION_CANCEL -> {
                mMenuState = ConstPool.STATE_DOCKED
                updateMenuByState()
                run {
                    mTouchStartY = 0f
                    mTouchStartX = mTouchStartY
                }
                false
            }

            else -> false
        }
    }

    override fun setX(x: Float) {
        var x = x
        if (x < 0) {
            x = 0f
        }
        super.setX(x)
    }

    override fun setY(y: Float) {
        var y = y
        if (y < 0) {
            y = 0f
        }
        super.setY(y)
    }

    interface OnMenuClickListener {
        fun onRefresh()
        fun onClose()
    }
}