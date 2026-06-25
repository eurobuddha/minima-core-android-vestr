package org.minimarex.vestr;

import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Collector tab — your vesting contracts; open one to collect what has vested. */
public class CollectorView extends BaseView {

    private final LinearLayout container;

    public CollectorView(MainActivity a) {
        super(a, R.layout.view_page);
        container = find(R.id.pageContainer);
        refresh();
    }

    @Override public void onShown() { act.requestReload(); refresh(); }

    @Override
    public void refresh() {
        container.removeAllViews();

        TextView h = new TextView(act);
        h.setText("Collect");
        h.setTextColor(VestrDesign.TEXT);
        h.setTextSize(22f);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setPadding(0, 0, 0, dp(6));
        container.addView(h);

        TextView blurb = new TextView(act);
        blurb.setText("Open a contract to withdraw the tokens that have vested.");
        blurb.setTextColor(VestrDesign.DIM);
        blurb.setTextSize(14f);
        blurb.setPadding(0, 0, 0, dp(16));
        container.addView(blurb);

        List<Contract> contracts = act.contracts();
        if (contracts.isEmpty()) {
            TextView empty = new TextView(act);
            empty.setText("Nothing to collect yet.");
            empty.setTextColor(VestrDesign.DIM);
            empty.setTextSize(14f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, 0);
            container.addView(empty);
        } else {
            for (Contract c : contracts) container.addView(ContractUi.card(act, c));
        }
    }
}
