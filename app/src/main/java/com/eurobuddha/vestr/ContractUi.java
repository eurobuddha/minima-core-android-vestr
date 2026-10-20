package com.eurobuddha.vestr;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

/** Shared rendering for a vesting-contract list card (Creator + Collector tabs), faithful to the dapp. */
public final class ContractUi {

    private ContractUi() {} // utility holder — never instantiated

    /** Build one tappable contract card (header icon/amount + two-column body) that opens the detail screen on click. */
    public static View card(final MainActivity act, final Contract c) {
        int pad = dp(act, 14);
        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(VestrDesign.CARD);
        bg.setCornerRadius(dp(act, 8));
        card.setBackground(bg);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.bottomMargin = dp(act, 12);
        card.setLayoutParams(clp);

        // ---- header: icon + nickname + amount ----
        LinearLayout head = new LinearLayout(act);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setBackgroundColor(VestrDesign.CARD_2);
        head.setPadding(pad, pad, pad, pad);

        // Avatar glyph: "M" for native Minima, else first letter of the token name (or "?" when unnamed).
        TextView icon = new TextView(act);
        icon.setText(c.isMinima() ? "M" : (c.tokenName.isEmpty() ? "?" : c.tokenName.substring(0, 1).toUpperCase()));
        icon.setTextColor(VestrDesign.ON_YELLOW);
        icon.setTextSize(18f);
        icon.setTypeface(Typeface.DEFAULT_BOLD);
        icon.setGravity(Gravity.CENTER);
        GradientDrawable ib = new GradientDrawable();
        ib.setColor(VestrDesign.YELLOW);
        ib.setCornerRadius(dp(act, 6));
        icon.setBackground(ib);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(act, 44), dp(act, 44));
        ilp.rightMargin = dp(act, 12);
        icon.setLayoutParams(ilp);
        head.addView(icon);

        LinearLayout titleCol = new LinearLayout(act);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        // No user nickname in the model — label by the FULL contract uid (never truncated); tap copies it.
        TextView nick = new TextView(act);
        nick.setText("Contract " + c.uid);
        nick.setTextColor(VestrDesign.DIM);
        nick.setTextSize(12f);
        nick.setSingleLine(false);
        nick.setOnClickListener(v -> {   // consumes the click so the card's detail listener doesn't fire
            ((ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("Contract id", c.uid));
            Toast.makeText(act, "Copied", Toast.LENGTH_SHORT).show();
        });
        titleCol.addView(nick);
        TextView amt = new TextView(act);
        amt.setText(amount(c.amount) + " " + c.tokenName);
        amt.setTextColor(VestrDesign.TEXT);
        amt.setTextSize(16f);
        amt.setTypeface(Typeface.DEFAULT_BOLD);
        titleCol.addView(amt);
        head.addView(titleCol);
        card.addView(head);

        // ---- body: two columns ----
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setPadding(pad, pad, pad, pad);

        LinearLayout left = new LinearLayout(act);
        left.setOrientation(LinearLayout.VERTICAL);
        left.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        left.addView(kv(act, "Total vested", amount(c.total), true));
        left.addView(kv(act, "Collected", amount(c.collected()), true));
        body.addView(left);

        LinearLayout right = new LinearLayout(act);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        right.setGravity(Gravity.END);
        // Right column: collect cadence (grace period) plus human-readable start/end timing.
        right.addView(rightLine(act, "Collect " + grace(c.graceHours), VestrDesign.DIM));
        right.addView(rightLine(act, relative(c.startMs, "start"), VestrDesign.DIM_2));
        right.addView(rightLine(act, relative(c.endMs, "end"), VestrDesign.DIM_2));
        body.addView(right);
        card.addView(body);

        // Tapping the card hands the raw coin JSON to the detail/collect activity.
        card.setOnClickListener(v -> {
            Intent i = new Intent(act, ContractDetailActivity.class);
            i.putExtra("coin", c.raw.toString());
            act.startActivity(i);
        });
        return card;
    }

    // ---- helpers ----

    /** "key: value" label line for the body's left column; {@code bold} emphasises headline figures. */
    private static View kv(MainActivity act, String k, String v, boolean bold) {
        TextView t = new TextView(act);
        t.setText(k + ": " + v);
        t.setTextColor(VestrDesign.TEXT);
        t.setTextSize(13f);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, dp(act, 2), 0, dp(act, 2));
        return t;
    }

    /** End-aligned single text line for the body's right (timing) column, in the given dim colour. */
    private static View rightLine(MainActivity act, String s, int color) {
        TextView t = new TextView(act);
        t.setText(s);
        t.setTextColor(color);
        t.setTextSize(12f);
        t.setGravity(Gravity.END);
        t.setPadding(0, dp(act, 2), 0, dp(act, 2));
        return t;
    }

    /** Map a grace-period in hours to its human label (e.g. "daily", "weekly") via the contract's Grace enum. */
    static String grace(int hours) {
        return VestingContract.Grace.fromHours(hours).label;
    }

    /** Format a token amount: cap at 6 decimals (round down — never overstate), drop trailing zeros. */
    static String amount(BigDecimal b) {
        try {
            BigDecimal t = b;
            if (b.scale() > 6) t = b.setScale(6, RoundingMode.DOWN);
            return t.stripTrailingZeros().toPlainString();
        } catch (Exception e) { return b.toPlainString(); }
    }

    /** Non-tappable dim row telling the user that {@code n} coins at the script address were refused by the safety gate. */
    public static View hiddenRow(MainActivity act, int n) {
        TextView t = new TextView(act);
        t.setText(n + (n == 1 ? " contract hidden" : " contracts hidden") + " — on-chain state is unreadable by this app.");
        t.setTextColor(VestrDesign.DIM_2);
        t.setTextSize(12f);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(act, 8), 0, dp(act, 8));
        return t;
    }

    /** "starts in 3 days" / "started 2 days ago" / "ends in …". */
    static String relative(long whenMs, String verb) {
        if (whenMs <= 0) return "";
        long diff = whenMs - System.currentTimeMillis();
        boolean past = diff < 0;
        String v = (verb.equals("start") ? (past ? "started " : "starts ") : (past ? "ended " : "ends "));
        long a = Math.abs(diff);
        long days = TimeUnit.MILLISECONDS.toDays(a);
        long hours = TimeUnit.MILLISECONDS.toHours(a);
        long mins = TimeUnit.MILLISECONDS.toMinutes(a);
        String span = days > 0 ? days + (days == 1 ? " day" : " days")
                : hours > 0 ? hours + (hours == 1 ? " hour" : " hours")
                : mins + (mins == 1 ? " min" : " mins");
        return v + (past ? span + " ago" : "in " + span);
    }

    /** Convert density-independent pixels to raw pixels using the device's display density. */
    private static int dp(MainActivity act, int v) {
        return (int) (v * act.getResources().getDisplayMetrics().density);
    }
}
