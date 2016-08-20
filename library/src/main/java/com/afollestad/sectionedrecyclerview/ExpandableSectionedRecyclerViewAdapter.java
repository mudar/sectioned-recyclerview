package com.bamstrategy.salewhale.ui.widget;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * SectionedRecyclerViewAdapter with support for expandable sections
 */
public abstract class ExpandableSectionedRecyclerViewAdapter<VH extends RecyclerView.ViewHolder> extends SectionedRecyclerViewAdapter {
    private static final String KEY_SAVED_STATE_EXPANDED = "expandable_adapter_state";
    private static final int SECTION_HEADER_OFFSET = 1;

    private Set<Integer> mExpandedSections;

    public ExpandableSectionedRecyclerViewAdapter() {
        this.mExpandedSections = new HashSet<>();

        shouldShowHeadersForEmptySections(true);
    }

    /**
     * Should the list be expanded initially
     *
     * @return
     */
    abstract protected boolean expandSectionsOnStart();

    /**
     * Get the number of items of selected section.
     * Replaces getItemCount()
     *
     * @param section
     * @return
     */
    abstract public int getSectionsItemCount(int section);

    protected void notifySectionsDataSetChanged() {
        mExpandedSections.clear();
        if (expandSectionsOnStart()) {
            final int count = getSectionCount();
            for (int i = 0; i < count; i++) {
                mExpandedSections.add(i);
            }
        }

        notifyDataSetChanged();
    }

    protected void notifySectionItemsInserted(int section) {
        if (expandSectionsOnStart()) {
            mExpandedSections.add(section);
        }

        notifyItemRangeInserted(getSectionAbsolutePosition(section), getSectionsItemCount(section));
    }

    protected void notifySectionRemoved(int section) {
        // Intended unnecessary boxing to avoid possible bugs by confusing
        // remove(int location) and remove(Integer object)
        final Integer sectionPosition = Integer.valueOf(section);
        mExpandedSections.remove(sectionPosition);

        notifyItemRangeRemoved(getSectionAbsolutePosition(section), getSectionsItemCount(section));
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, final int section) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleExpandableSection(section);
            }
        });
    }

    /**
     * This overrides the parent method to handle expanded/reduced state.
     * Extending classes must implemet getSectionsItemCount() instead
     *
     * @param section
     * @return
     */
    @Override
    final public int getItemCount(int section) {
        return isSectionExpanded(section) ? getSectionsItemCount(section) : 0;
    }

    /**
     * Cal this on the activity/fragment onRestoreInstanceState() to save the sections' restore
     * expanded/reduced sections on device rotation.
     * Also requires a call to onSaveInstanceState()
     *
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        final ArrayList<Integer> list = savedInstanceState.getIntegerArrayList(KEY_SAVED_STATE_EXPANDED);
        if (list != null) {
            mExpandedSections = new HashSet<>(list);
        }
    }

    /**
     * Cal this on the activity/fragment onSaveInstanceState() to save the sections' restore
     * expanded/reduced sections on device rotation.
     * Also requires a call to onRestoreInstanceState()
     *
     * @param outState
     */
    public void onSaveInstanceState(Bundle outState) {
        outState.putIntegerArrayList(KEY_SAVED_STATE_EXPANDED, new ArrayList<>(mExpandedSections));
    }

    protected void toggleExpandableSection(int section) {
        if (isSectionExpanded(section)) {
            reduceSection(section, getSectionAbsolutePosition(section));
        } else {
            expandSection(section, getSectionAbsolutePosition(section));
        }
    }

    protected int getSectionAbsolutePosition(int section) {
        int absolutePosition = 0;
        for (int i = 0; i < section; i++) {
            absolutePosition += getItemCount(i) + SECTION_HEADER_OFFSET; // Add offset header of the section
        }

        return absolutePosition;
    }

    protected boolean isSectionExpanded(int section) {
        return mExpandedSections.contains(section);
    }

    private void expandSection(int section, int absolutePosition) {
        mExpandedSections.add(section);

        // Update the section header itself
        notifyItemChanged(absolutePosition);
        // Show the newly added section items
        notifyItemRangeInserted(absolutePosition + SECTION_HEADER_OFFSET, getSectionsItemCount(section));
    }

    private void reduceSection(int section, int absolutePosition) {
        // Intended unnecessary boxing to avoid possible bugs by confusing
        // remove(int location) and remove(Integer object)
        final Integer sectionPosition = Integer.valueOf(section);

        mExpandedSections.remove(sectionPosition);

        // Update the section header itself
        notifyItemChanged(absolutePosition);
        // Remove the hidden section items
        notifyItemRangeRemoved(absolutePosition + SECTION_HEADER_OFFSET, getSectionsItemCount(section));
    }
}
