# JCalendar [![](https://jitpack.io/v/GGJJack/JCalendar.svg)](https://jitpack.io/#GGJJack/JCalendar)

## Add Dependency

### Project Gradle

```Gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Module Gradle

```Gradle
dependencies {
    implementation 'com.github.GGJJack:JCalendar:1.0.4'
}
```

## Usage

### Add XML

```xml
<com.hstudio.jcalendarview.JCalendarView
    android:id="@+id/calendar"
    android:layout_width="0dp"
    android:layout_height="0dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

### Adapter

```kotlin
abstract class JCalendarAdapter<ViewHolder : JCalendarViewHolder, HeaderViewHolder : JCalendarViewHolder> {

    abstract fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewHolder

    abstract fun onBindViewHolder(holder: ViewHolder, x: Int, y: Int, date: Date)

    abstract fun onCreateHeaderView(layoutInflater: LayoutInflater, parent: ViewGroup): HeaderViewHolder

    abstract fun onBindHeaderViewHolder(holder: HeaderViewHolder, dayOfWeek: Date)

    open fun getViewType(x: Int, y: Int): Int = 0
}
```

### ViewHolder

```kotlin
abstract class JCalendarViewHolder(private val _rootView: View) {
    var viewType: Int = 0

    val view: View get() = _rootView

    open fun hasFocusView(){}

    open fun lostFocusView(){}
}
```

### Code

```kotlin
// Start
calendar.setAdapter(adapter)

// Move Month
adapter.setYearAndMonth(2020, 1) // Move to target year and month
adapter.setYear(2020) // Move to target year
adapter.setMonth(1) // Move to target month
adapter.beforeMonth() //Move to before month
adapter.nextMonth() // Move to next month

// Get View Holder
val viewHolderFromDate = adapter.getViewHolder(Date()) //Get ViewHolder from date
val viewHolderFromXY = adapter.getViewHolder(1, 1) //Get ViewHolder from XY position

// Get Date
val currentMonthDate: Date = adapter.getDate() // Same adapter.getTargetDate()
val targetDate: Date = adapter.getTargetDate() // Same adapter.getDate()

val focusDate: Date? = adapter.getFocusDate() // Cursor Date
val calendarStartDate: Date? = adapter.getCalendarStartDate() // Calendar start date
val calendarEndDate: Date? = adapter.getCalendarEndDate() // Calendar end date
val dateFromPosition = adapter.getDateFromXY(1, 1) //Get date from XY position

// Position
val positionFromDate: Pair<Int, Int>? = adapter.getXYFromDate(Date()) // Position from date
val focusXY: Pair<Int, Int>? = adapter.getFocusXY() // Position from current focus

// Focus Change
adapter.clearFocus() // Clear focus
adapter.focusStartDay() // Focus start day
adapter.focusMonthStartDay() // Focus month start day
adapter.focusPreviewDay() // Focus preview day
adapter.focusNextDay() // Focus next day
adapter.focusEndDay() // Focus end day

// Listener
interface MonthChangeListener {
    fun monthChanged(focusDate: Date)
}

adapter.setMonthChangeListener(object: MonthChangeListener {
    override fun monthChanged(focusDate: Date) {
        TODO("Not yet implemented")
    }
})
```
