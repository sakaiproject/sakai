/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.calendar.api;

import org.sakaiproject.time.api.TimeRange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * <p>A collection for managing calendar events that provides list-based storage and time range filtering.</p>
 * <p>This class enables efficient bulk loading of calendar events while maintaining the ability to filter and access
 * events within specific time ranges. It can store events from various sources including iterators and collections,
 * making it ideal for handling calendar data in service layer operations.</p>
 * <p>As a List implementation, it provides all the functionality of a List, including positional access and manipulation
 * of elements, as well as additional methods for filtering events by time range.</p>
 * <p>Events are automatically stored in order of their TimeRange, with events having a null TimeRange placed at the end.
 * This ordering is maintained for all add and addAll operations, regardless of the specified index.</p>
 */
public class CalendarEventList implements List<CalendarEvent> {
    private ArrayList<CalendarEvent> events;

    /**
     * Comparator for sorting CalendarEvent objects by their TimeRange.
     * Events with null TimeRange are placed at the end.
     */
    private static final Comparator<CalendarEvent> EVENT_COMPARATOR = (e1, e2) -> {
        TimeRange r1 = e1.getRange();
        TimeRange r2 = e2.getRange();

        // If both ranges are null, consider them equal
        if (r1 == null && r2 == null) {
            return 0;
        }

        // If only the first range is null, it goes after the second
        if (r1 == null) {
            return 1;
        }

        // If only the second range is null, it goes after the first
        if (r2 == null) {
            return -1;
        }

        // Both ranges are non-null, compare by firstTime
        return r1.firstTime().compareTo(r2.firstTime());
    };

    /**
     * Constructs a new empty CalendarEventList
     */
    public CalendarEventList() {
        this.events = new ArrayList<>();
    }

    /**
     * Constructs a new CalendarEventList from a collection of events,
     * maintaining order by TimeRange. Events with null TimeRange are placed at the end.
     * @param events Collection of CalendarEvent objects to initialize the list with
     */
    public CalendarEventList(Collection<CalendarEvent> events) {
        this.events = new ArrayList<>();
        addAll(events);
    }


    /**
     * Constructs a new CalendarEventList from an Iterator of events,
     * maintaining order by TimeRange. Events with null TimeRange are placed at the end.
     * @param events Iterator of CalendarEvent objects to initialize the list with
     */
    public CalendarEventList(Iterator<CalendarEvent> events) {
        this.events = new ArrayList<>();
        while (events.hasNext()) {
            add(events.next());
        }
    }

    /**
     * Returns the element at the specified position in this list.
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public CalendarEvent get(int index) {
        return this.events.get(index);
    }

    /**
     * Replaces the element at the specified position in this list with the specified element.
     * @param index index of the element to replace
     * @param element element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public CalendarEvent set(int index, CalendarEvent element) {
        return this.events.set(index, element);
    }

    /**
     * Inserts the specified element in this list, maintaining order by TimeRange.
     * Events with null TimeRange are placed at the end.
     * The index parameter is ignored as the position is determined by the TimeRange order.
     * @param index index at which the specified element is to be inserted (ignored)
     * @param element element to be inserted
     */
    @Override
    public void add(int index, CalendarEvent element) {
        // Ignore the index parameter and maintain order by TimeRange
        add(element);
    }

    /**
     * Removes the element at the specified position in this list.
     * @param index the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public CalendarEvent remove(int index) {
        return this.events.remove(index);
    }

    /**
     * Returns the index of the first occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     * @param o element to search for
     * @return the index of the first occurrence of the specified element in this list,
     *         or -1 if this list does not contain the element
     */
    @Override
    public int indexOf(Object o) {
        return this.events.indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified element in this list,
     * or -1 if this list does not contain the element.
     * @param o element to search for
     * @return the index of the last occurrence of the specified element in this list,
     *         or -1 if this list does not contain the element
     */
    @Override
    public int lastIndexOf(Object o) {
        return this.events.lastIndexOf(o);
    }

    /**
     * Returns a list iterator over the elements in this list (in proper sequence).
     * @return a list iterator over the elements in this list (in proper sequence)
     */
    @Override
    public java.util.ListIterator<CalendarEvent> listIterator() {
        return this.events.listIterator();
    }

    /**
     * Returns a list iterator over the elements in this list (in proper sequence),
     * starting at the specified position in the list.
     * @param index index of the first element to be returned from the list iterator
     * @return a list iterator over the elements in this list (in proper sequence),
     *         starting at the specified position in the list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public java.util.ListIterator<CalendarEvent> listIterator(int index) {
        return this.events.listIterator(index);
    }

    /**
     * Returns a view of the portion of this list between the specified fromIndex,
     * inclusive, and toIndex, exclusive.
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     */
    @Override
    public List<CalendarEvent> subList(int fromIndex, int toIndex) {
        return this.events.subList(fromIndex, toIndex);
    }

    /**
     * Inserts all of the elements in the specified collection into this list,
     * maintaining order by TimeRange. Events with null TimeRange are placed at the end.
     * The index parameter is ignored as the position is determined by the TimeRange order.
     * @param index index at which to insert the first element from the specified collection (ignored)
     * @param c collection containing elements to be added to this list
     * @return true if this list changed as a result of the call
     */
    @Override
    public boolean addAll(int index, Collection<? extends CalendarEvent> c) {
        // Ignore the index parameter and maintain order by TimeRange
        return addAll(c);
    }

    /**
     * Add an event to the collection, maintaining order by TimeRange.
     * Events with null TimeRange are placed at the end.
     * @param event The event to add.
     * @return true if the event was added successfully.
     */
    @Override
    public boolean add(CalendarEvent event) {
        // Find the insertion point to maintain order using binary search
        int insertionPoint = findInsertionPoint(event);

        // Insert at the appropriate position
        this.events.add(insertionPoint, event);
        return true;
    }

    /**
     * Find the insertion point for an event using binary search.
     * This is more efficient than linear search for large lists.
     * @param event The event to find an insertion point for.
     * @return The index where the event should be inserted.
     */
    private int findInsertionPoint(CalendarEvent event) {
        int low = 0;
        int high = this.events.size() - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1; // Avoid potential overflow
            CalendarEvent midEvent = this.events.get(mid);
            int cmp = EVENT_COMPARATOR.compare(midEvent, event);

            if (cmp <= 0) {
                low = mid + 1; // Look in the right half
            } else {
                high = mid - 1; // Look in the left half
            }
        }

        return low; // 'low' is the insertion point
    }

    /**
     * Get an iterator over all events in the collection.
     * @return An iterator over all events.
     */
    @Override
    public Iterator<CalendarEvent> iterator() {
        return this.events.iterator();
    }

    /**
     * Check if the collection is empty.
     * @return true if the collection is empty.
     */
    @Override
    public boolean isEmpty() {
        return this.events.isEmpty();
    }

    /**
     * Get the size of the collection.
     * @return The number of events in the collection.
     */
    @Override
    public int size() {
        return this.events.size();
    }

    /**
     * Check if the collection contains the specified element.
     * @param o The element to check for.
     * @return true if the collection contains the element.
     */
    @Override
    public boolean contains(Object o) {
        return this.events.contains(o);
    }

    /**
     * Check if the collection contains all elements in the specified collection.
     * @param c The collection to check for.
     * @return true if the collection contains all elements in the specified collection.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return this.events.containsAll(c);
    }

    /**
     * Add all elements from the specified collection to this collection,
     * maintaining order by TimeRange. Events with null TimeRange are placed at the end.
     * @param c The collection to add.
     * @return true if this collection changed as a result of the call.
     */
    @Override
    public boolean addAll(Collection<? extends CalendarEvent> c) {
        boolean modified = false;
        for (CalendarEvent event : c) {
            modified |= add(event);
        }
        return modified;
    }

    /**
     * Remove all elements from the collection.
     */
    @Override
    public void clear() {
        this.events.clear();
    }

    /**
     * Remove the specified element from the collection.
     * @param o The element to remove.
     * @return true if the collection contained the element.
     */
    @Override
    public boolean remove(Object o) {
        return this.events.remove(o);
    }

    /**
     * Remove all elements in the specified collection from this collection.
     * @param c The collection of elements to remove.
     * @return true if this collection changed as a result of the call.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return this.events.removeAll(c);
    }

    /**
     * Retain only the elements in this collection that are contained in the specified collection.
     * @param c The collection of elements to retain.
     * @return true if this collection changed as a result of the call.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        return this.events.retainAll(c);
    }

    /**
     * Convert the collection to an array.
     * @return An array containing all elements in the collection.
     */
    @Override
    public Object[] toArray() {
        return this.events.toArray();
    }

    /**
     * Convert the collection to an array of the specified type.
     * @param a The array into which the elements of the collection are to be stored.
     * @return An array containing all elements in the collection.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return this.events.toArray(a);
    }

    /**
     * Return an iterator on events in the CalendarEventVector.
     * The order in which the events will be found in the iteration is by event start date.
     * @param range A time range to limit the iterated events.  May be null; all events will be returned.
     * @return an iterator on CalendarEvent objects in the CalendarEventVector (may be empty).
     */
    public List<CalendarEvent> eventsInRange(TimeRange range) {
        return this.events.stream()
                .filter(event -> range != null && range.overlaps(event.getRange()))
                .toList();
    }

    /**
     * Return an iterator on events in the CalendarEventVector.
     * The order in which the events will be found in the iteration is by event start date.
     * @param range A time range to limit the iterated events.  May be null; all events will be returned.
     * @return an iterator on CalendarEvent objects in the CalendarEventVector (may be empty).
     */
    public Iterator<CalendarEvent> eventsInRangeIterator(TimeRange range) {
        return eventsInRange(range).iterator();
    }
}
