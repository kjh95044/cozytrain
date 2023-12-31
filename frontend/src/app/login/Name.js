import styles from "./Name.module.css";

export default function Name() {
  return (
    <div className={styles.body}>
      <h1 className={styles.h1}>
        <span className={styles.sp}>칙</span>
        <span className={styles.sp}>칙</span>
      </h1>
      <h1 className={styles.h2}>
        <span className={styles.sp}>포</span>
        <span className={styles.sp}>근</span>
        <span className={styles.sp}>포</span>
        <span className={styles.sp}>근</span>
      </h1>
    </div>
  );
}
