package com.easycoach.easyloyalty;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by muoki on 1/3/2016.
 */
public class InfoAdapter extends RecyclerView.Adapter <InfoAdapter.MyViewHolder> {
    private LayoutInflater inflater;
    List <Information> data = Collections.emptyList();
    private Context context;
    MyClickListener myClickListener;

    public InfoAdapter(Context context, List<Information> data)
    {
        inflater=LayoutInflater.from(context);
        this.data = data;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder,final int position) {
        Information currentObject = data.get(position);

        holder.icon.setImageResource(currentObject.iconId);
        holder.title.setText(currentObject.tittle);

        /*holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(position);
            }
        });*/

    }

    public void setMyClickListener (MyClickListener myClickListener)
    {
        this.myClickListener=myClickListener;
    }

    /*public void delete(int position)
    {
        data.remove(position);
        notifyItemRemoved(position);
    }*/

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView title;
        ImageView icon;
        public MyViewHolder(View itemView)
        {

            super(itemView);

            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.listText);
            icon = (ImageView) itemView.findViewById(R.id.listIcon);

        }

        @Override
        public void onClick(View v)
        {
            //context.startActivity(new Intent(context, SubActivity.class));
            if (myClickListener!=null)
            {
                myClickListener.myItemClicked(v, getPosition());
            }
        }
    }

    public interface MyClickListener
    {
        void myItemClicked(View view, int position);
    }
}
