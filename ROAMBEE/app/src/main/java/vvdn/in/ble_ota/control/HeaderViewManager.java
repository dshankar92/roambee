package vvdn.in.ble_ota.control;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import vvdn.in.ble_ota.AndroidAppUtils;
import vvdn.in.ble_ota.R;
import vvdn.in.ble_ota.listener.HeaderViewClickListener;


/****************************************************************************
 * HeaderViewManager.java
 * Created on May 25, 2016
 * Copyright (c) 2014, NETGEAR, Inc.
 * 350 East Plumeria, San Jose California, 95134, U.S.A.
 * All rights reserved.
 * This software is the confidential and proprietary information of
 * NETGEAR, Inc. ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with NETGEAR.
 *
 * @author VVDN
 *         Class Name - HeaderViewManager
 *         Description - This class is used to manage the header view of
 *         every activity. Basically this is the controller class which will
 *         be binded to every class which will be containing header.
 *****************************************************************************/
public class HeaderViewManager {

    /**
     * Instance of this class
     */
    public static HeaderViewManager mHeaderManagerInstance;
    /**
     * Debugging TAG
     */
    private String TAG = HeaderViewManager.class.getSimpleName();

    /**
     * Header View Instance
     */
    private RelativeLayout header_strip, headerLeftView, headerRightView, mRlImageRight;
    private TextView headerHeadingText, headerSubHeadingText;
    private TextView headerLeftText, headerRightText;
    private ImageView headerLeftImage, headerRightImage;
    private View grey_line_header;
    private ProgressBar api_loading_request;

    /**
     * Instance of Header View Manager
     *
     * @return
     */
    public static HeaderViewManager getInstance() {
        if (mHeaderManagerInstance == null) {
            mHeaderManagerInstance = new HeaderViewManager();
        }

        return mHeaderManagerInstance;
    }

    /**
     * Initialize Header View
     *
     * @param mActivity
     * @param mView
     * @param headerViewClickListener
     */
    public void InitializeHeaderView(Activity mActivity, View mView, boolean isWhite,
                                     HeaderViewClickListener headerViewClickListener) {
        if (mActivity != null) {
            header_strip = (RelativeLayout) mActivity.findViewById(R.id.header_strip);
            grey_line_header = (View) mActivity.findViewById(R.id.grey_line_header);
            if (isWhite) {
                header_strip.setBackgroundColor(mActivity.getResources().getColor(R.color.black));
                grey_line_header.setVisibility(View.VISIBLE);
            } /*else {
                header_strip.setBackground(mActivity.getResources().getDrawable(R.drawable.bluetoth));
                grey_line_header.setVisibility(View.GONE);
            }*/
            headerLeftView = (RelativeLayout) mActivity.findViewById(R.id.image_left_rl);
            headerRightView = (RelativeLayout) mActivity.findViewById(R.id.image_right_rl);
            mRlImageRight = (RelativeLayout) mActivity.findViewById(R.id.image_right_rl_rl);
            headerHeadingText = (TextView) mActivity.findViewById(R.id.header_title);

            if (isWhite) {
                headerHeadingText.setTextColor(mActivity.getResources().getColor(R.color.orange));
            } else {
//                headerHeadingText.setTextColor(mActivity.getResources().getColor(R.color.white));
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.white));
            }
            headerLeftText = (TextView) mActivity.findViewById(R.id.headerLeftTextView);
            headerRightText = (TextView) mActivity.findViewById(R.id.headerRightTextView);
            headerLeftImage = (ImageView) mActivity.findViewById(R.id.image_left);
            headerRightImage = (ImageView) mActivity.findViewById(R.id.image_right);
            api_loading_request = (ProgressBar) mActivity.findViewById(R.id.api_loading_request);
        } else if (mView != null) {
            header_strip = (RelativeLayout) mView.findViewById(R.id.header_strip);
            grey_line_header = (View) mView.findViewById(R.id.grey_line_header);
            if (isWhite) {
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.white_six));
                grey_line_header.setVisibility(View.VISIBLE);
            } else {
//                header_strip.setBackground(mView.getResources().getDrawable(R.drawable.header_black));
                grey_line_header.setVisibility(View.GONE);
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.black));
            }
            headerLeftView = (RelativeLayout) mView.findViewById(R.id.image_left_rl);
            headerRightView = (RelativeLayout) mView.findViewById(R.id.image_right_rl);
            headerHeadingText = (TextView) mView.findViewById(R.id.header_title);
            mRlImageRight = (RelativeLayout) mView.findViewById(R.id.image_right_rl_rl);
            if (isWhite) {
                headerHeadingText.setTextColor(mView.getResources().getColor(R.color.card_bg));
            } else {
//                headerHeadingText.setTextColor(mView.getResources().getColor(R.color.white));
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.black));
            }
            headerRightText = (TextView) mView.findViewById(R.id.headerRightTextView);
            headerLeftText = (TextView) mView.findViewById(R.id.headerLeftTextView);
            headerLeftImage = (ImageView) mView.findViewById(R.id.image_left);
            headerRightImage = (ImageView) mView.findViewById(R.id.image_right);
            api_loading_request = (ProgressBar) mView.findViewById(R.id.api_loading_request);
        }
        manageClickOnViews(headerViewClickListener);
    }

    /**
     * Initialize Header View
     *
     * @param mActivity
     * @param mView
     * @param headerViewClickListener
     */
    public void InitializeMultiTitleHeaderView(Activity mActivity, View mView, boolean isWhite,
                                               HeaderViewClickListener headerViewClickListener) {
        if (mActivity != null) {
            header_strip = (RelativeLayout) mActivity.findViewById(R.id.header_strip);
            grey_line_header = (View) mActivity.findViewById(R.id.grey_line_header);
            if (isWhite) {
                header_strip.setBackgroundColor(mActivity.getResources().getColor(R.color.white_six));
                grey_line_header.setVisibility(View.VISIBLE);
            } else {
//                header_strip.setBackground(mActivity.getResources().getDrawable(R.drawable.header_black));
                header_strip.setBackgroundColor(mActivity.getResources().getColor(R.color.black));
                grey_line_header.setVisibility(View.GONE);
            }
            headerLeftView = (RelativeLayout) mActivity.findViewById(R.id.image_left_rl);
            headerRightView = (RelativeLayout) mActivity.findViewById(R.id.image_right_rl);
            headerHeadingText = (TextView) mActivity.findViewById(R.id.header_title);
            headerSubHeadingText = (TextView) mActivity.findViewById(R.id.header_subtitle);
            mRlImageRight = (RelativeLayout) mActivity.findViewById(R.id.image_right_rl_rl);
            if (isWhite) {
                headerHeadingText.setTextColor(mActivity.getResources().getColor(R.color.header_text_darkgrey));
            } else {
                headerHeadingText.setTextColor(mActivity.getResources().getColor(R.color.orange));
            }
            headerLeftText = (TextView) mActivity.findViewById(R.id.headerLeftTextView);
            headerRightText = (TextView) mActivity.findViewById(R.id.headerRightTextView);
            headerLeftImage = (ImageView) mActivity.findViewById(R.id.image_left);
            headerRightImage = (ImageView) mActivity.findViewById(R.id.image_right);
            api_loading_request = (ProgressBar) mActivity.findViewById(R.id.api_loading_request);
        } else if (mView != null) {
            header_strip = (RelativeLayout) mView.findViewById(R.id.header_strip);
            if (isWhite) {
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.white_six));
            } else {
//                header_strip.setBackground(mView.getResources().getDrawable(R.drawable.header_black));
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.black));
            }
            headerLeftView = (RelativeLayout) mView.findViewById(R.id.image_left_rl);
            headerRightView = (RelativeLayout) mView.findViewById(R.id.image_right_rl);
            headerHeadingText = (TextView) mView.findViewById(R.id.header_title);
            if (isWhite) {
                headerHeadingText.setTextColor(mView.getResources().getColor(R.color.header_text_darkgrey));
            } else {
//                headerHeadingText.setTextColor(mView.getResources().getColor(R.color.white));
                header_strip.setBackgroundColor(mView.getResources().getColor(R.color.black));
            }
            headerSubHeadingText = (TextView) mView.findViewById(R.id.header_subtitle);
            headerRightText = (TextView) mView.findViewById(R.id.headerRightTextView);
            headerLeftText = (TextView) mView.findViewById(R.id.headerLeftTextView);
            headerLeftImage = (ImageView) mView.findViewById(R.id.image_left);
            headerRightImage = (ImageView) mView.findViewById(R.id.image_right);
            api_loading_request = (ProgressBar) mView.findViewById(R.id.api_loading_request);
        }
        manageClickOnViews(headerViewClickListener);
    }

    /**
     * ManageClickOn Header view
     *
     * @param headerViewClickListener
     */
    private void manageClickOnViews(
            final HeaderViewClickListener headerViewClickListener) {
        // Click on Header Left View
        headerLeftView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                headerViewClickListener.onClickOfHeaderLeftView();
            }
        });
        // Click on Header Right View
        headerRightView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                headerViewClickListener.onClickOfHeaderRightView();
            }
        });
    }

    /**
     * Set Heading View Text
     *
     * @param isVisible
     * @param headingStr
     */
    public void setHeading(boolean isVisible, String headingStr) {
        if (headerHeadingText != null) {
            if (isVisible) {
                headerHeadingText.setVisibility(View.VISIBLE);
                headerHeadingText.setText(headingStr);
            } else {
                headerHeadingText.setVisibility(View.GONE);
            }
        } else {
            AndroidAppUtils.showErrorLog(TAG,
                    "Header Heading Text View is null");
        }
    }

    /**
     * Set Heading View Text
     *
     * @param isVisible
     * @param headingStr
     */
    public void setHeading(boolean isVisible, String headingStr, Activity activity) {
        if (headerHeadingText != null) {
            if (isVisible) {
                headerHeadingText.setVisibility(View.VISIBLE);
                if (headingStr != null && !headingStr.isEmpty()) {
                    if (headingStr.length() > 15) {
                        AndroidAppUtils.showLog(TAG, "setHeading if");
                        headerHeadingText.setEllipsize(TextUtils.TruncateAt.END);
                        headerHeadingText.setWidth((int) activity.getResources().getDimension(R.dimen.width_dev_info_name_unclaimed));
                    } else {
                        AndroidAppUtils.showLog(TAG, "setHeading else");
                        ViewGroup.LayoutParams params = headerHeadingText.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        headerHeadingText.setLayoutParams(params);
                    }
                    headerHeadingText.setText(headingStr);
                }
            } else {
                headerHeadingText.setVisibility(View.GONE);
            }
        } else {
            AndroidAppUtils.showErrorLog(TAG,
                    "Header Heading Text View is null");
        }
    }

    /**
     * Set Heading View Text
     *
     * @param isVisible
     * @param headingStr
     * @param activity
     * @param visibleCharCount
     */
    public void setHeading(boolean isVisible, String headingStr, Activity activity, int visibleCharCount) {
        if (headerHeadingText != null) {
            if (isVisible) {
                headerHeadingText.setVisibility(View.VISIBLE);
                if (headingStr != null && !headingStr.isEmpty()) {
                    if (headingStr.length() > visibleCharCount) {
                        AndroidAppUtils.showLog(TAG, "setHeading if");
                        headerHeadingText.setEllipsize(TextUtils.TruncateAt.END);
                        headerHeadingText.setWidth((int) activity.getResources().getDimension(R.dimen.width_dev_info_name_unclaimed));
                    } else {
                        AndroidAppUtils.showLog(TAG, "setHeading else");
                        ViewGroup.LayoutParams params = headerHeadingText.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        headerHeadingText.setLayoutParams(params);
                    }
                    headerHeadingText.setText(headingStr);
                }
            } else {
                headerHeadingText.setVisibility(View.GONE);
            }
        } else {
            AndroidAppUtils.showErrorLog(TAG,
                    "Header Heading Text View is null");
        }
    }

    /**
     * Set Sub Heading View Text
     *
     * @param isVisible
     * @param headingStr
     */
    public void setSubHeading(boolean isVisible, String headingStr) {

        if (headerSubHeadingText != null) {
            if (isVisible) {
                headerSubHeadingText.setVisibility(View.VISIBLE);
                headerSubHeadingText.setText(headingStr);
            } else {
                headerSubHeadingText.setVisibility(View.GONE);
            }
        } else {
            AndroidAppUtils.showErrorLog(TAG,
                    "Header Heading Text View is null");
        }
    }

    /**
     * Manage Header Left View
     *
     * @param isVisibleImage
     * @param isVisibleText
     * @param ImageId
     * @param LeftString
     */
    public void setLeftSideHeaderView(boolean isVisibleImage,
                                      boolean isVisibleText, int ImageId, String LeftString) {
        if (!isVisibleImage && !isVisibleText) {
            headerLeftView.setVisibility(View.INVISIBLE);
        } else if (headerLeftView == null || headerLeftText == null
                || headerLeftImage == null) {
            AndroidAppUtils.showErrorLog(TAG, "Header Left View is null");
        } else if (isVisibleImage) {
            headerLeftText.setVisibility(View.GONE);
            headerLeftView.setVisibility(View.VISIBLE);
            headerLeftImage.setVisibility(View.VISIBLE);
            if (ImageId > 0) {
                headerLeftImage.setImageResource(ImageId);
            } else {
                AndroidAppUtils.showErrorLog(TAG,
                        "Header left image id is null");
            }

        } else if (isVisibleText) {
            headerLeftText.setVisibility(View.VISIBLE);
            headerLeftView.setVisibility(View.VISIBLE);
            headerLeftImage.setVisibility(View.GONE);
            if (LeftString != null && !LeftString.isEmpty()) {
                headerLeftText.setText(LeftString);
            } else {
                AndroidAppUtils.showErrorLog(TAG,
                        "Header left header string is null");
            }
        }

    }

    /**
     * Set Header Right Side View
     *
     * @param isVisibleImage
     * @param isVisibleText
     * @param ImageId
     * @param RightString
     */
    public void setRightSideHeaderView(boolean isVisibleImage,
                                       boolean isVisibleText, int ImageId, String RightString) {
        if (!isVisibleImage && !isVisibleText) {
            headerRightView.setVisibility(View.INVISIBLE);
        } else if (headerRightView == null || headerRightText == null
                || headerRightImage == null) {
            AndroidAppUtils.showErrorLog(TAG, "Header Right View is null");
        } else if (isVisibleImage) {
            headerRightText.setVisibility(View.GONE);
            headerRightView.setVisibility(View.VISIBLE);
            headerRightImage.setVisibility(View.VISIBLE);
            if (ImageId > 0) {
                headerRightImage.setImageResource(ImageId);
            } else {
                headerRightImage.setVisibility(View.GONE);
            }

        } else if (isVisibleText) {
            headerRightText.setVisibility(View.VISIBLE);
            headerRightImage.setVisibility(View.GONE);
            headerRightView.setVisibility(View.VISIBLE);
            if (RightString != null && !RightString.isEmpty()) {
                headerRightText.setText(RightString);
            } else {
                AndroidAppUtils.showErrorLog(TAG,
                        "Header Right header string is null");
            }
        }
    }


    /**
     * This method is to set the heading text size dynamically
     */
    public void setHeadingTextSize(float textSize) {
        if (headerHeadingText != null)
            headerHeadingText.setTextSize(textSize);
    }

    /**
     * This method is to set the get api loader
     */
    public void setProgressLoader(boolean loaderVisible, boolean isMultiViewHeader) {
        if (!isMultiViewHeader) {
            if (loaderVisible) {
                api_loading_request.setVisibility(View.VISIBLE);
                mRlImageRight.setVisibility(View.GONE);
            } else {
                api_loading_request.setVisibility(View.GONE);
                mRlImageRight.setVisibility(View.VISIBLE);
            }
        } else {
            if (loaderVisible) {
                api_loading_request.setVisibility(View.VISIBLE);
            } else {
                api_loading_request.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Method used to enable disable right header view
     *
     * @param isEnabled
     */
    public void setHeaderRightViewEnabled(boolean isEnabled) {
        if (isEnabled) {
            headerRightView.setEnabled(true);
        } else {
            headerRightView.setEnabled(false);
        }
    }

    /**
     * Method used to change the header background color
     *
     * @param color
     */
    public void setHeaderBackgroundColor(int color) {
        if (header_strip != null) {
            header_strip.setBackgroundColor(color);
        }
    }

    /*******************************************************************
     * Function name - hideHeaderLine
     * Description - For hiding the gray line just below to the header.
     *******************************************************************/
    public void hideHeaderLine() {
        if (grey_line_header != null) {
            grey_line_header.setVisibility(View.GONE);
        }
    }
    /*******************************************************************
     * Function name - changeRightImageViewColor
     * Description - For changing the icon color of button clicked
     *******************************************************************/
    public void changeRightImageViewColor(int color)
    {
        headerRightImage.setColorFilter(color);
    }
}

