package com.baiyjk.shopping.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baiyjk.shopping.OrderConfirmActivity;
import com.baiyjk.shopping.R;
import com.baiyjk.shopping.adapter.CartListSimpleAdapter;
import com.baiyjk.shopping.http.HttpFactory;
import com.baiyjk.shopping.utils.PatchByLoginStatus;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ShoppingcartFragment extends ListFragment{
	private Context mContext;
	private String cartJson;
	private TextView mTv;
	private Button backButton;
	private Button clearButton;
	private TextView cartSizeTv;
	private TextView cartValueTv;
	private final String TAG = "Shoppingcart Activity";
	private int cartSize;
	private float cartValue;

	private Map<String, Object> map;
	private Button editButton;
	private List<Map<String, Object>> list;

	private final String CART_STATE_NORMAL = "cart_state_normal";
	private final String CART_STATE_EDIT = "cart_state_edit";
	private String mState = CART_STATE_NORMAL;
	private Button finishButton;
	private CartListSimpleAdapter adapter;
	private View checkoutButton;
	private ListView ListView;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.shoppingcart, container, false);
		initView(v);
		return v;
	}

	private void initView(View v) {
		mContext = v.getContext();
		mTv = (TextView) v.findViewById(R.id.cart_list_loading);
		backButton = (Button) v.findViewById(R.id.cart_back);
		editButton = (Button) v.findViewById(R.id.cart_edit);
		
		finishButton = (Button) v.findViewById(R.id.cart_finish);
		clearButton = (Button) v.findViewById(R.id.cart_clear);
		cartSizeTv = (TextView) v.findViewById(R.id.cart_size);
		cartValueTv = (TextView) v.findViewById(R.id.cart_value);
		checkoutButton = (View) v.findViewById(R.id.cart_checkout_button);
		ListView = (ListView)v.findViewById(android.R.id.list);
		//后退按钮
		backButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		
		//去结算
		checkoutButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Log.d("购物车", "去结算");
				//PC中依次检查各商品的最大购买数量/ajax/checkProNum.do；
				
				//TODO 考虑重复提交请求的问题
				//如果已登录，跳转到OrderConfirmActivity
				//如果没登录，跳转到LoginActivity

				PatchByLoginStatus patchObj = new PatchByLoginStatus(mContext, OrderConfirmActivity.class);
				patchObj.patch();
			}
		});
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				cartJson = HttpFactory.getHttp().getRequest(
						"/showCart.do/?format=true", mContext);
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (cartJson != null) {
							mTv.setVisibility(View.GONE);
							Log.d(TAG, cartJson);
							adapter = new CartListSimpleAdapter(
									mContext, getData(),
									R.layout.shoppingcart_item, new String[] {"productId", "url", "name", "qty", "price","image" }, 
										new int[] {
											R.id.cart_product_id,
											R.id.cart_product_url,
											R.id.cart_product_name,
											R.id.cart_product_edittext,
											R.id.cart_product_price,
											R.id.cart_product_image });
							
							setListAdapter(adapter);
							// Log.d(TAG, strContext);
						} else
							mTv.setText("加载失败......");
					}
				});
			}
		}).start();

		// edit the cart.删除购物车中某个商品
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mState.equals(CART_STATE_NORMAL)) {
//					clearButton.setVisibility(View.VISIBLE);
					editButton.setVisibility(View.GONE);
					finishButton.setVisibility(View.VISIBLE);
					
					mState = CART_STATE_EDIT;
					
				}
				adapter.setEditState(mState);
				adapter.notifyDataSetChanged();
			}
		});

		// clear the cart
		clearButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		//退出编辑状态
		finishButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d(TAG, "click finish");
				mState = CART_STATE_NORMAL;
//				clearButton.setVisibility(View.GONE);
				editButton.setVisibility(View.VISIBLE);
				finishButton.setVisibility(View.GONE);
				adapter.setEditState(mState);
				adapter.notifyDataSetChanged();
			}
		});
	}

	private List<Map<String, Object>> getData() {
		list = new ArrayList<Map<String, Object>>();

		try {
			JSONArray cartJsonArray = new JSONArray(cartJson);
			// 暂时只取['row']['0']
			JSONObject listJsonObject = cartJsonArray.getJSONObject(0)
					.getJSONObject("row").getJSONObject("0");

			Log.d(TAG, listJsonObject.toString());
			Iterator<String> iterator = listJsonObject.keys();
			for (int i = 0; i < listJsonObject.length(); i++) {
				String productId = iterator.next();
				JSONObject product = listJsonObject.getJSONObject(productId);
				JSONObject result = product.getJSONObject("result");
				map = new HashMap<String, Object>();
				map.put("productId", productId);
				map.put("url", result.get("link"));
				map.put("name", result.get("name"));
				map.put("qty", result.get("qty"));
				map.put("price", "￥" + result.get("price"));
				map.put("image", result.get("imageUrl"));
				map.put("editstate", CART_STATE_NORMAL);
				list.add(map);
			}

//			map = new HashMap<String, Object>();
//			map.put("editstate", mState);//默认不展示删除按钮，状态值保存在list的最后一项
//			list.add(map);

			cartSize = cartJsonArray.getJSONObject(0).getInt("qty");
			cartValue = cartJsonArray.getJSONObject(0).getLong("price");

			cartSizeTv.setText("共 " + cartSize + " 件");
			cartValueTv.setText("￥ " + cartValue);

			Log.d(TAG, map.toString());
		} catch (JSONException e) {
			// TODO: handle exception
			Log.d("shoppingcart debug", e.getMessage());
		}

		return list;
	}
}
