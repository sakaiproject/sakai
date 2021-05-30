package org.sakaiproject.time.impl;

import java.time.Instant;
import java.util.StringTokenizer;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeRange;

import lombok.extern.slf4j.Slf4j;


/**********************************************************************************************************************************************************************************************************************************************************
 * TimeRange implementation
 *********************************************************************************************************************************************************************************************************************************************************/
@Slf4j
public class MyTimeRange implements TimeRange
{
    private transient BasicTimeService timeService;
    // ends included?
    protected boolean m_startIncluded = true;

    protected boolean m_endIncluded = true;

    // start and end times
    protected Time m_startTime = null;

    protected Time m_endTime = null;

    /**
     * construct from a two times, and start and end inclusion booleans
     *
     * @param start:
     *        start time
     * @param end:
     *        end time
     * @param startIncluded:
     *        true if start is part of the range
     * @param endIncluded:
     *        true of end is part of the range
     * @deprecated
     */
    public MyTimeRange(BasicTimeService timeService, Time start, Time end, boolean startIncluded, boolean endIncluded)
    {
        this.timeService = timeService;
        m_startTime = start;
        m_endTime = end;
        m_startIncluded = startIncluded;
        m_endIncluded = endIncluded;

        // start time must be <= end time
        if (m_startTime.getTime() > m_endTime.getTime())
        {
            // reverse them to fix
            Time t = m_startTime;
            m_startTime = m_endTime;
            m_endTime = t;
        }
    } // TimeRange
    
    public MyTimeRange(BasicTimeService timeService, Instant start, Instant end, boolean startIncluded, boolean endIncluded)
    {
        this.timeService = timeService;
        Time startTime = timeService.newTime(start.toEpochMilli());
        Time endTime = timeService.newTime(end.toEpochMilli());
        new MyTimeRange(timeService, startTime, endTime, startIncluded, endIncluded);
        
    }

    /**
     * construct from a string, in our format
     *
     * @param str
     *        the time range string
     */
    public MyTimeRange(BasicTimeService timeService, String str)
    {
        this.timeService = timeService;
        parse(str);
    } // TimeRange


    /**
     * construct from a time long and a duration long in ms
     *
     * @param start
     *        time value
     * @param duration
     *        ms duration
     */
    public MyTimeRange(BasicTimeService timeService, long start, long duration)
    {
        this.timeService = timeService;
        m_startTime = timeService.newTime(start);
        m_endTime = timeService.newTime(start + duration);
        m_startIncluded = true;
        m_endIncluded = true;

        // start time must be <= end time
        if (m_startTime.getTime() > m_endTime.getTime())
        {
            // reverse them to fix
            Time t = m_startTime;
            m_startTime = m_endTime;
            m_endTime = t;
        }

    } // TimeRange


    @Override
    public boolean contains(Time time)
    {
        // assume in range, unless proven otherwise
        boolean inRange = true;

        // if out of the range...
        if (time.before(m_startTime) || time.after(m_endTime))
        {
            inRange = false;
        }

        // if at begin and begin not in range
        else if ((!m_startIncluded) && time.equals(m_startTime))
        {
            inRange = false;
        }

        // if at the end and end not in range
        else if ((!m_endIncluded) && time.equals(m_endTime))
        {
            inRange = false;
        }

        return inRange;

    } // contains
    
    @Override
    public boolean contains(Instant instant) {
        Time time = timeService.newTime(instant.toEpochMilli());
        return contains(time);
    }
    
    @Override
    public boolean overlaps(TimeRange range)
    {
        boolean overlaps = false;

        // null range?

        // if my start is in the other range
        if (range.contains(firstTime()))
        {
            overlaps = true;
        }

        // if my end is in the other range
        else if (range.contains(lastTime()))
        {
            overlaps = true;
        }

        // if I contain the other range
        else if (contains(range))
        {
            overlaps = true;
        }

        return overlaps;

    } // overlaps

    /**
     * do I completely contain this other range?
     *
     * @param range:
     *        the time range to check for containment
     * @return true if range is within my time ramge
     */
    public boolean contains(TimeRange range)
    {
        // I must contain both is start and end
        return (contains(range.firstTime()) && contains(range.lastTime()));

    } // contains

    @Override
    public Time firstTime()
    {
        return firstTime(1);

    } // firstTime

    @Override
    public Time lastTime()
    {
        return lastTime(1);

    } // lastTime
    
    @Override
    public Instant firstInstant() {
        return firstTime(1).toInstant();
    }

    @Override
    public Instant lastInstant() {
        return lastTime(1).toInstant();
    }


    @Override
    public Time firstTime(long fudge)
    {
        // if the start is included, return this
        if (m_startIncluded)
        {
            return m_startTime;
        }

        // if not, return a time one ms after start
        Time fudgeStartTime = (Time) m_startTime.clone();
        fudgeStartTime.setTime(m_startTime.getTime() + fudge);
        return fudgeStartTime;

    } // firstTime

    @Override
    public Instant firstInstant(long fudge) {
        return firstTime(fudge).toInstant();
    }

    @Override
    public Time lastTime(long fudge)
    {
        // if the end is included, return this
        if (m_endIncluded)
        {
            return m_endTime;
        }

        // if not, return a time one ms before end
        Time fudgeEndTime = (Time) m_endTime.clone();
        fudgeEndTime.setTime(m_endTime.getTime() - fudge);
        return fudgeEndTime;

    } // lastTime

    @Override
    public Instant lastInstant(long fudge) {
        return lastTime(fudge).toInstant();
    }

    @Override
    public String toString()
    {
        // a place to build the string (slightly larger)
        StringBuilder buf = new StringBuilder(64);

        // start with the start value, always used
        buf.append(m_startTime);

        // more that single value?
        if (!m_startTime.equals(m_endTime))
        {
            // what separator to use?
            if (m_startIncluded && m_endIncluded)
            {
                buf.append('-');
            }
            else if ((!m_startIncluded) && (!m_endIncluded))
            {
                buf.append('~');
            }
            else if (!m_startIncluded)
            {
                buf.append('[');
            }
            else
            {
                buf.append(']');
            }

            // add the end
            buf.append(m_endTime);
        }

        // return the answer as a string
        return buf.toString();

    } // toString

    @Override
    public String toStringHR()
    {
        // a place to build the string (slightly larger)
        StringBuilder buf = new StringBuilder(64);

        // start with the start value, always used
        buf.append(m_startTime.toStringGmtFull());

        // more that single value?
        if (!m_startTime.equals(m_endTime))
        {
            // what separator to use?
            if (m_startIncluded && m_endIncluded)
            {
                buf.append(" - ");
            }
            else if ((!m_startIncluded) && (!m_endIncluded))
            {
                buf.append(" ~ ");
            }
            else if (!m_startIncluded)
            {
                buf.append(" [ ");
            }
            else
            {
                buf.append(" ] ");
            }

            // add the end
            buf.append(m_endTime.toStringGmtFull());
        }

        // return the answer as a string
        return buf.toString();

    } // toStringHR

    @Override
    public boolean equals(Object obj)
    {
        boolean equals = false;

        if (obj instanceof MyTimeRange)
        {
            equals = ((((MyTimeRange) obj).m_startIncluded == m_startIncluded)
                    && (((MyTimeRange) obj).m_endIncluded == m_endIncluded)
                    && (((MyTimeRange) obj).m_startTime.equals(m_startTime)) && (((MyTimeRange) obj).m_endTime
                            .equals(m_endTime)));
        }

        return equals;

    } // equals

    @Override
    public int hashCode() {
        String hash = Boolean.toString(m_startIncluded) + Boolean.toString(m_endIncluded)
        + m_startTime.getDisplay() + m_endTime.getDisplay();
        return hash.hashCode();
    }

    @Override
    public long duration()
    {
        // KNL-1536, SAK-30793, SAK-23076 - Get the *actual* duration - Ignore fudging
        return (lastTime(0).getTime() - firstTime(0).getTime());

    } // duration

    /**
     * parse from a string - resolve fully earliest ('!') and latest ('*') and durations ('=')
     *
     * @param str
     *        the string to parse
     * @param earliest
     *        Time to substitute for any 'earliest' values
     * @param latest
     *        the Time to use for 'latest'
     */
    protected void parse(String str)
    {
        try
        {
            // separate the string by '[]~-'
            // (we do want the delimiters as tokens, thus the true param)
            StringTokenizer tokenizer = new StringTokenizer(str, "[]~-", true);

            int tokenCount = 0;
            long startMs = -1;
            long endMs = -1;
            m_startTime = null;
            m_endTime = null;

            while (tokenizer.hasMoreTokens())
            {
                tokenCount++;
                String next = tokenizer.nextToken();

                switch (tokenCount)
                {
                case 1:
                {
                    if (next.charAt(0) == '=')
                    {
                        // use the rest as a duration in ms
                        startMs = Long.parseLong(next.substring(1));
                    }

                    else
                    {
                        m_startTime = timeService.newTimeGmt(next);
                    }

                }
                break;

                case 2:
                {
                    // set the inclusions
                    switch (next.charAt(0))
                    {
                    // start not included
                    case '[':
                    {
                        m_startIncluded = false;
                        m_endIncluded = true;

                    }
                    break;

                    // end not included
                    case ']':
                    {
                        m_startIncluded = true;
                        m_endIncluded = false;

                    }
                    break;

                    // neither included
                    case '~':
                    {
                        m_startIncluded = false;
                        m_endIncluded = false;

                    }
                    break;

                    // both included
                    case '-':
                    {
                        m_startIncluded = true;
                        m_endIncluded = true;

                    }
                    break;

                    // trouble!
                    default:
                    {
                        throw new Exception();
                    }
                    } // switch (next[0])

                }
                break;

                case 3:
                {
                    if (next.charAt(0) == '=')
                    {
                        // use the rest as a duration in ms
                        endMs = Long.parseLong(next.substring(1));
                    }

                    else
                    {
                        m_endTime = timeService.newTimeGmt(next);
                    }

                }
                break;

                // trouble!
                default:
                {
                    throw new Exception();
                }
                } // switch (tokenCount)

            } // while (tokenizer.hasMoreTokens())

            // if either start or end was in duration, adjust (but not both!)
            if ((startMs != -1) && (endMs != -1))
            {
                throw new Exception("==");
            }

            if (startMs != -1)
            {
                if (m_endTime == null)
                {
                    throw new Exception();
                }
                m_startTime = timeService.newTime(m_endTime.getTime() - startMs);
            }
            else if (endMs != -1)
            {
                if (m_startTime == null)
                {
                    throw new Exception("=, ! null");
                }
                m_endTime = timeService.newTime(m_startTime.getTime() + endMs);
            }

            // if there is only one token
            if (tokenCount == 1)
            {
                // end is start, both included
                m_endTime = m_startTime;
                m_startIncluded = true;
                m_endIncluded = true;
            }

            // start time must be <= end time
            if (m_startTime.getTime() > m_endTime.getTime())
            {
                // reverse them to fix
                Time t = m_startTime;
                m_startTime = m_endTime;
                m_endTime = t;
            }
        }
        catch (Exception e)
        {
            log.warn("parse: exception parsing: {} : {}", str, e.toString());

            // set a now range, just to have something
            m_startTime = timeService.newTime();
            m_endTime = m_startTime;
            m_startIncluded = true;
            m_endIncluded = true;
        }
    }

    @Override
    public void shiftBackward(long i)
    {
        m_startTime.setTime(m_startTime.getTime() - i);
        m_endTime.setTime(m_endTime.getTime() - i);
    }

    @Override
    public void shiftForward(long i)
    {
        m_startTime.setTime(m_startTime.getTime() + i);
        m_endTime.setTime(m_endTime.getTime() + i);
    }

    @Override
    public void zoom(double f)
    {
        long oldRange = m_endTime.getTime() - m_startTime.getTime();
        long center = m_startTime.getTime() + oldRange / 2;
        long newRange = (long) ((double) oldRange * f);

        m_startTime.setTime(center - newRange / 2);
        m_endTime.setTime(center + newRange / 2);
    }

    @Override
    public void adjust(TimeRange original, TimeRange modified)
    {
        if (original.equals(modified)) return;

        // adjust for the change in the start time
        m_startTime.setTime(m_startTime.getTime()
                + (((MyTimeRange) modified).m_startTime.getTime() - ((MyTimeRange) original).m_startTime.getTime()));

        // adjust for the change in the end time
        m_endTime.setTime(m_endTime.getTime()
                + (((MyTimeRange) modified).m_endTime.getTime() - ((MyTimeRange) original).m_endTime.getTime()));

    } // adjust

    @Override
    public boolean isSingleTime()
    {
        return (m_startTime.equals(m_endTime) && m_startIncluded && m_endIncluded);

    } // isSingleTime
    
    @Override
    public Object clone()
    {
        TimeRange obj = new MyTimeRange(timeService, (Time) m_startTime.clone(), (Time) m_endTime.clone(), m_startIncluded, m_endIncluded);

        return obj;

    } // clone
} // class TimeRange

