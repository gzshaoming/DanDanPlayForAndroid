package com.xyoye.dandanplay.ui.activities.personal;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.ui.activities.WebViewActivity;
import com.xyoye.dandanplay.ui.weight.dialog.BiliBiliDownloadDialog;
import com.xyoye.dandanplay.utils.CommonUtils;

import butterknife.BindView;

/**
 * Created by xyoye on 2018/7/28.
 */

public class DownloadBiliBiliActivity extends BaseMvcActivity implements View.OnClickListener {
    public final static int SELECT_WEB = 106;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.av_code_tv)
    TextView avCodeTv;
    @BindView(R.id.bv_code_tv)
    TextView bvCodeTv;
    @BindView(R.id.url_tv)
    TextView urlTv;
    @BindView(R.id.select_url_rl)
    RelativeLayout selectUrlRelayout;
    @BindView(R.id.av_code_et)
    EditText avCodeEt;
    @BindView(R.id.bv_code_et)
    EditText bvCodeEt;
    @BindView(R.id.url_et)
    EditText urlEt;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_download_bilibili;
    }

    @Override
    public void initPageView() {
        setTitle("BiliBili弹幕下载");
    }

    @Override
    public void initPageViewListener() {
        avCodeTv.setOnClickListener(this);
        bvCodeTv.setOnClickListener(this);
        urlTv.setOnClickListener(this);
        selectUrlRelayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_code_tv:
                String avNumber = avCodeEt.getText().toString();
                if (avNumber.isEmpty()) {
                    ToastUtils.showShort("AV号不能为空");
                } else if (!CommonUtils.isNum(avNumber)) {
                    ToastUtils.showShort("请输入纯数字AV号");
                } else {
                    new BiliBiliDownloadDialog(
                            DownloadBiliBiliActivity.this,
                            R.style.Dialog,
                            avNumber,
                            BiliBiliDownloadDialog.BILIBILI_AV
                    ).show();
                }
                break;
            case R.id.bv_code_tv:
                String bvNumber = bvCodeEt.getText().toString();
                if (bvNumber.isEmpty()) {
                    ToastUtils.showShort("BV号不能为空");
                } else if (bvNumber.length() != 12) {
                    ToastUtils.showShort("请输入12位BV号");
                } else {
                    new BiliBiliDownloadDialog(
                            DownloadBiliBiliActivity.this,
                            R.style.Dialog,
                            bvNumber,
                            BiliBiliDownloadDialog.BILIBILI_BV
                    ).show();
                }
                break;
            case R.id.url_tv:
                String urlLink = urlEt.getText().toString();
                if (urlLink.isEmpty()) {
                    ToastUtils.showShort("视频链接不能为空");
                    return;
                }
                if (!urlLink.startsWith("http")) {
                    urlLink = "https://" + urlLink;
                }
                if (!CommonUtils.isUrlLink(urlLink)) {
                    ToastUtils.showShort("请输入正确视频链接");
                } else {
                    new BiliBiliDownloadDialog(
                            DownloadBiliBiliActivity.this,
                            R.style.Dialog,
                            urlLink,
                            BiliBiliDownloadDialog.BILIBILI_URL
                    ).show();
                }
                break;
            case R.id.select_url_rl:
                Intent intent = new Intent(DownloadBiliBiliActivity.this, WebViewActivity.class);
                intent.putExtra("title", "选择链接");
                intent.putExtra("link", "http://www.bilibili.com");
                intent.putExtra("isSelect", true);
                startActivityForResult(intent, SELECT_WEB);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_WEB && data != null) {
                String selectUrl = data.getStringExtra("selectUrl");
                if (selectUrl.isEmpty()) {
                    ToastUtils.showShort("视频链接不能为空");
                } else if (!CommonUtils.isUrlLink(selectUrl)) {
                    ToastUtils.showShort("请输入正确视频链接");
                } else {
                    new BiliBiliDownloadDialog(
                            DownloadBiliBiliActivity.this,
                            R.style.Dialog,
                            selectUrl,
                            BiliBiliDownloadDialog.BILIBILI_URL
                    ).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
