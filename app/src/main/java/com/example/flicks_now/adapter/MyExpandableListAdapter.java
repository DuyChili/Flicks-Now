package com.example.flicks_now.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.flicks_now.R;

import java.util.HashMap;
import java.util.List;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> listHeaders;
    private HashMap<String, List<String>> listChildren;

    public MyExpandableListAdapter(Context context, List<String> listHeaders, HashMap<String, List<String>> listChildren) {
        this.context = context;
        this.listHeaders = listHeaders;
        this.listChildren = listChildren;
    }

    @Override
    public int getGroupCount() {
        return listHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<String> children = listChildren.get(listHeaders.get(groupPosition));
        return children != null ? children.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listChildren.get(listHeaders.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView headerTextView = convertView.findViewById(R.id.listHeader);
        headerTextView.setText(headerTitle);

        // Kiểm tra nếu mục hiện tại có mục con không
        ImageView expandIcon = convertView.findViewById(R.id.expandIcon); // Biểu tượng mở rộng tùy chỉnh
        if (getChildrenCount(groupPosition) == 0) {
            // Ẩn biểu tượng mở rộng nếu không có con
            expandIcon.setVisibility(View.INVISIBLE);
        } else {
            // Hiển thị biểu tượng mở rộng cho các mục có con
            expandIcon.setVisibility(View.VISIBLE);
            expandIcon.setImageResource(isExpanded ? R.drawable.ic_drop_up_24 : R.drawable.baseline_arrow_drop_down_24); // biểu tượng tuỳ chỉnh
        }

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }

        TextView childTextView = convertView.findViewById(R.id.listItem);
        childTextView.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Object getChildren(int groupPosition) {
        return listChildren.get(listHeaders.get(groupPosition));
    }
}
