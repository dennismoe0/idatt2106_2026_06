import { describe, it, expect, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import IntroView from '@/views/student/IntroView.vue'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/intro', name: 'Intro', component: IntroView },
    { path: '/', name: 'Home', component: { template: '<div>Home</div>' } },
  ],
})

describe('IntroView', () => {
  beforeEach(async () => {
    localStorage.clear()
    await router.push('/intro')
    await router.isReady()
  })

  it('shows the mayor and idrettspark story instead of the generic placeholder intro', () => {
    const wrapper = mount(IntroView, {
      global: { plugins: [router] },
    })

    expect(wrapper.text()).toContain('ordfører')
    expect(wrapper.text()).toContain('idrettspark')
    expect(wrapper.text()).toContain('nettdetektiv')
  })
})
