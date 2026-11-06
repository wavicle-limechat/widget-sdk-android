package ai.limechat.widget.characterization

import ai.limechat.widget.LimechatWidgetView
import ai.limechat.widget.models.WidgetConfig
import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * Describes the close behaviour of jitpack `v0.0.9-alpha` (tag commit 253523a, from the
 * refactor/back-button-client-integration line), which a client runs today. Ported into
 * chore/sdk-drift-cleanup by plan task b0 (LimechatWidgetView.handleCloseEvent).
 *
 * 0.0.9-alpha routes `close-widget` (and the since-removed `widget-back` and `limechat://close`)
 * through handleCloseEvent():
 *   - showLegacyBackIcon = true and the view's context is an Activity -> activity.finish(),
 *     host onClose() is NOT called
 *   - showLegacyBackIcon = true and the context is not an Activity -> onClose()
 *   - showLegacyBackIcon = true and activity.finish() throws -> onClose()
 *   - showLegacyBackIcon = false -> onClose() only (same as develop)
 */
@RunWith(RobolectricTestRunner::class)
class AlphaV009CloseBehaviourTest {

    @Before
    fun setUp() {
        tokenMemoryCache().clear()
    }

    private fun activityView(legacy: Boolean, cb: RecordingCallback): Pair<Activity, LimechatWidgetView> {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val v = LimechatWidgetView(activity)
        v.init(WidgetConfig.builder("tok").setShowLegacyBackIcon(legacy).build(), cb)
        return activity to v
    }

    @Test
    fun `alpha - legacy back icon + activity context - close-widget finishes the activity and skips onClose`() {
        val cb = RecordingCallback()
        val (activity, v) = activityView(legacy = true, cb = cb)
        v.postFromJs("""{"event":"close-widget","type":"close-widget"}""")
        assertTrue(activity.isFinishing)
        assertEquals(emptyList<String>(), cb.events)
    }

    @Test
    fun `alpha - legacy back icon + activity context + no callback - close still finishes the activity`() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        val v = LimechatWidgetView(activity)
        v.init(WidgetConfig.builder("tok").setShowLegacyBackIcon(true).build())
        v.postFromJs("""{"event":"close-widget"}""")
        assertTrue(activity.isFinishing)
    }

    @Test
    fun `alpha - legacy back icon + non-activity context - falls back to onClose`() {
        val cb = RecordingCallback()
        val v = LimechatWidgetView(RuntimeEnvironment.getApplication())
        v.init(WidgetConfig.builder("tok").setShowLegacyBackIcon(true).build(), cb)
        v.postFromJs("""{"event":"close-widget"}""")
        assertEquals(listOf("onClose"), cb.events)
    }

    class ThrowingFinishActivity : Activity() {
        override fun finish() {
            throw IllegalStateException("finish failed")
        }
    }

    @Test
    fun `alpha - legacy back icon + finish() throws - falls back to onClose`() {
        val cb = RecordingCallback()
        val activity = Robolectric.buildActivity(ThrowingFinishActivity::class.java).setup().get()
        val v = LimechatWidgetView(activity)
        v.init(WidgetConfig.builder("tok").setShowLegacyBackIcon(true).build(), cb)
        v.postFromJs("""{"event":"close-widget"}""")
        assertEquals(listOf("onClose"), cb.events)
    }

    @Test
    fun `alpha - no legacy back icon - onClose only, activity keeps running`() {
        val cb = RecordingCallback()
        val (activity, v) = activityView(legacy = false, cb = cb)
        v.postFromJs("""{"event":"close-widget"}""")
        assertEquals(listOf("onClose"), cb.events)
        assertFalse(activity.isFinishing)
    }

    @Test
    fun `alpha - legacy back icon does not add show_legacy_back_icon to the url`() {
        val (_, v) = activityView(legacy = true, cb = RecordingCallback())
        assertEquals(
            "https://app.limechat.ai/widget?website_token=tok&platform=android&locale=en&color_scheme=light",
            v.lastLoadedUrl()
        )
    }
}
