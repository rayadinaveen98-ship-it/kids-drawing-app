from pathlib import Path

path = Path('app/src/test/java/com/navin/kidsdrawing/lesson/session/LessonSessionEngineTest.kt')
text = path.read_text()
old = '''    @Test
    fun traceModeRetainsPreparingStepBoundaryAcrossAllPacesUntilP24() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine()

            val result = engine.dispatch(
                LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, pace),
            )

            assertTrue(result is LessonCommandResult.Accepted)
            val state = engine.state as LessonSessionState.PreparingStep
            assertEquals(TeachingMode.TRACE_AND_LEARN, state.context.mode)
            assertEquals(pace, state.context.pace)
            assertEquals(0, state.context.currentStepIndex)
            assertEquals("head", state.context.currentStepId)
            assertTrue(state.context.overviewCompleted)
        }
    }
'''
new = '''    @Test
    fun traceModeStartsTeacherImmediatelyAcrossAllPaces() {
        TeachingPace.entries.forEach { pace ->
            val engine = engine()

            val result = engine.dispatch(
                LessonCommand.StartLesson(TeachingMode.TRACE_AND_LEARN, pace),
            )

            assertTrue(result is LessonCommandResult.Accepted)
            result as LessonCommandResult.Accepted
            val state = engine.state as LessonSessionState.TeacherDemonstrating
            assertEquals(TeachingMode.TRACE_AND_LEARN, state.context.mode)
            assertEquals(pace, state.context.pace)
            assertEquals(0, state.context.currentStepIndex)
            assertEquals("head", state.context.currentStepId)
            assertTrue(state.context.overviewCompleted)
            val request = result.events.filterIsInstance<TeacherPlaybackRequested>().single().request
            assertEquals("head", request.stepId)
            assertEquals(pace, request.pace)
        }
    }
'''
count = text.count(old)
if count != 1:
    raise SystemExit(f'Expected one compatibility block, found {count}')
path.write_text(text.replace(old, new, 1))
