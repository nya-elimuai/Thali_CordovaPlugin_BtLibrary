/* Copyright (c) 2015 Microsoft Corporation. This software is licensed under the MIT License.
 * See the license file delivered with this project for further information.
 */
package org.thaliproject.nativesample.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.thaliproject.nativesample.app.model.Connection;
import org.thaliproject.nativesample.app.model.PeerAndConnectionModel;
import org.thaliproject.nativesample.app.R;
import org.thaliproject.p2p.btconnectorlib.PeerProperties;

/**
 * A fragment containing the list of discovered peers.
 */
public class PeerListFragment extends Fragment implements PeerAndConnectionModel.Listener {
    public interface Listener {
        void onConnectRequest(PeerProperties peerProperties);
        void onSendDataRequest(PeerProperties peerProperties);
    }

    private static final String TAG = PeerListFragment.class.getName();
    private Context mContext = null;
    private ListView mListView = null;
    private ListAdapter mListAdapter = null;
    private PeerAndConnectionModel mModel = null;
    private Listener mListener = null;

    public PeerListFragment() {
    }

    public synchronized void setListener(Listener listener) {
        mListener = listener;

        if (mListener != null && mListView != null) {
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                    PeerProperties peerProperties = (PeerProperties)mListView.getItemAtPosition(index);

                    if (mListener != null) {
                        mListener.onConnectRequest(peerProperties);
                    } else {
                        Log.i(TAG, "onItemClick: " + peerProperties.toString() + " clicked, but I have no listener");
                    }
                }
            });

            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long l) {
                    PeerProperties peerProperties = (PeerProperties)mListView.getItemAtPosition(index);
                    Log.i(TAG, "onItemLongClick: " + peerProperties.toString());

                    if (mListener != null) {
                        mListener.onSendDataRequest(peerProperties);
                        return true; // Consumed
                    }

                    return false;
                }
            });

            mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
                    PeerProperties peerProperties = (PeerProperties)mListView.getItemAtPosition(index);
                    Log.i(TAG, "onItemSelected: " + peerProperties.toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_peers, container, false);

        mModel = PeerAndConnectionModel.getInstance();
        mContext = view.getContext();
        mListAdapter = new ListAdapter(mContext);

        mListView = (ListView)view.findViewById(R.id.listView);
        mListView.setAdapter(mListAdapter);
        setListener(mListener);

        mModel.setListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        mModel.setListener(null);
        super.onDestroy();
    }

    @Override
    public void onDataChanged() {
        Log.i(TAG, "onDataChanged");
        Handler handler = new Handler(mContext.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                mListAdapter.notifyDataSetChanged();
            }
        });
    }

    class ListAdapter extends BaseAdapter {
        private LayoutInflater mInflater = null;
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
            mInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mModel.getPeers().size();
        }

        @Override
        public Object getItem(int position) {
            return mModel.getPeers().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_peer, null);
            }

            PeerProperties peerProperties = mModel.getPeers().get(position);

            TextView textView = (TextView) view.findViewById(R.id.peerName);
            textView.setText(peerProperties.getName());

            textView = (TextView) view.findViewById(R.id.peerId);
            textView.setText(peerProperties.getId());

            boolean hasIncomingConnection = mModel.hasConnectionToPeer(peerProperties.getId(), true);
            boolean hasOutgoingConnection = mModel.hasConnectionToPeer(peerProperties.getId(), false);
            String connectionInformationText = "";

            if (hasIncomingConnection && hasOutgoingConnection) {
                connectionInformationText = "Connected (incoming and outgoing)";
            } else if (hasIncomingConnection) {
                connectionInformationText = "Connected (incoming)";
            } else if (hasOutgoingConnection) {
                connectionInformationText = "Connected (outgoing)";
            } else {
                connectionInformationText = "Not connected";
            }

            Connection connectionResponsibleForSendingData = null;

            if (hasOutgoingConnection) {
                connectionResponsibleForSendingData = mModel.getConnectionToPeer(peerProperties, false);
            } else if (hasIncomingConnection) {
                connectionResponsibleForSendingData = mModel.getConnectionToPeer(peerProperties, true);
            }

            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.sendDataProgressBar);
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);

            if (connectionResponsibleForSendingData != null
                    && connectionResponsibleForSendingData.isSendingData()) {
                progressBar.setProgress((int)(connectionResponsibleForSendingData.getSendDataProgress() * 100));

                connectionInformationText += ", sending "
                        + String.format("%.2f", connectionResponsibleForSendingData.getTotalDataAmountCurrentlySendingInMegaBytes())
                        + " MB (" + String.format("%.3f", connectionResponsibleForSendingData.getCurrentDataTransferSpeedInMegaBytesPerSecond())
                        + " MB/s)";
            } else {
                progressBar.setProgress(0);
            }

            textView = (TextView) view.findViewById(R.id.connectionsInformation);
            textView.setText(connectionInformationText);

            return view;
        }
    }
}
