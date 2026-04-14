<template>
  <main>
    <section>
      <h1>Velkommen, detektiv</h1>
      <p>
        Dette er introen for nye elever. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin tristique mauris eros, nec pharetra odio malesuada vel. Praesent consequat congue libero, sit amet accumsan dolor. Nulla facilisi. Maecenas dignissim elementum nibh eget tristique. Cras et lacus dignissim, pellentesque quam sit amet, congue urna. Vivamus lacinia pulvinar nisi, vel gravida eros iaculis ac. Duis pretium mauris purus. Mauris sed lectus lacinia, auctor ante ac, interdum mauris. Sed lobortis nisi sed massa sollicitudin, eget ultrices eros mattis. Etiam sed iaculis est. Praesent a commodo tellus.
      </p>

      <RouterLink to="/" @click="markIntroSeen">Gå til startsiden</RouterLink>
    </section>
  </main>
</template>

<script setup>
function markIntroSeen() {
  localStorage.setItem('hasSeenIntro', 'true')
}
</script>
