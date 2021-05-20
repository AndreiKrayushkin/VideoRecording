package by.andrei.firstproject.testapplication

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

class FragmentAnimation : Fragment(R.layout.fragment_writer){

    private lateinit var textAnimations: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textAnimations = view.findViewById(R.id.string_in_fragment)

        val animator = ValueAnimator.ofFloat(.0f, 450.0f).apply {
            /*т.к. 1 проход = 20 кадров, частота = 30 кадров, то время на 1 проход = 20/30 = 0.66 сек.*/
            duration = 666
            addUpdateListener { value -> textAnimations.x = value.animatedValue as Float }
            repeatMode = ValueAnimator.REVERSE
        }

        val set = AnimatorSet()

        set.playTogether(animator)

        set.addListener(object: Animator.AnimatorListener {
            var isCancelled = false
            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (isCancelled.not()) set.start()
            }

            override fun onAnimationCancel(animation: Animator?) {
                isCancelled = true
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
        set.start()
    }
}