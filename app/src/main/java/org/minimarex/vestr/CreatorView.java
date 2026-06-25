package org.minimarex.vestr;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Creator tab — Calculate / Create + your vesting contracts. */
public class CreatorView extends BaseView {

    private final LinearLayout container;

    public CreatorView(MainActivity a) {
        super(a, R.layout.view_page);
        container = find(R.id.pageContainer);
        refresh();
    }

    @Override public void onShown() { act.requestReload(); refresh(); }

    @Override
    public void refresh() {
        container.removeAllViews();

        container.addView(button("＋  Create a contract", true, v -> {
            Intent i = new Intent(act, CreateContractActivity.class);
            i.putExtra("script", act.scriptAddress());
            act.startActivity(i);
        }));
        container.addView(button("≈  Calculate a contract", false,
                v -> act.startActivity(new Intent(act, CalculatorActivity.class))));

        TextView h = new TextView(act);
        h.setText("My contracts");
        h.setTextColor(VestrDesign.TEXT);
        h.setTextSize(16f);
        h.setTypeface(Typeface.DEFAULT_BOLD);
        h.setPadding(0, dp(20), 0, dp(12));
        container.addView(h);

        List<Contract> contracts = act.contracts();
        if (contracts.isEmpty()) {
            TextView empty = new TextView(act);
            empty.setText("You have no vesting contracts yet.");
            empty.setTextColor(VestrDesign.DIM);
            empty.setTextSize(14f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(24), 0, 0);
            container.addView(empty);
        } else {
            for (Contract c : contracts) container.addView(ContractUi.card(act, c));
        }
    }

    private View button(String text, boolean primary, View.OnClickListener onClick) {
        TextView b = new TextView(act);
        b.setText(text);
        b.setTextColor(primary ? VestrDesign.ON_YELLOW : VestrDesign.TEXT_ON_DARK);
        b.setTextSize(15f);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(0, dp(14), 0, dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(primary ? VestrDesign.YELLOW : VestrDesign.BLACK);
        bg.setCornerRadius(dp(8));
        b.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(10);
        b.setLayoutParams(lp);
        b.setOnClickListener(onClick);
        return b;
    }
}
