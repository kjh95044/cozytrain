import { useState, useEffect } from "react";

import getFetch from "@/services/getFetch";
import styles from "./LetterContent.module.css";
import SleepTime from "./report/_components/SleepTime";
import Score from "./Score";
import Empty from "./report/_components/Empty";
import Walking from "@/components/Lottie/Walking";
import Coffee from "@/components/Lottie/Coffee";
import Meditation from "@/components/Lottie/Meditation";

export default function LetterContent() {
  const [averageReport, setAverageReport] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [report, setReport] = useState({});
  const [factor, setFactor] = useState(null);

  useEffect(() => {
    getReport();
  }, []);

  const handleSetFactor = (idx) => {
    setFactor(idx);
  };

  const getReport = async () => {
    try {
      const resp = await getFetch(`report`);

      setReport(resp.response.todayReport);
      setAverageReport(resp.response.averageReport);
    } catch (e) {
      console.log(e);
      setReport({});
    }

    setIsLoading(false);
  };

  return (
    <div className={styles.modal}>
      <div className={styles.title}>11월 14일 리포트</div>
      {!isLoading && report.date && (
        <>
          <SleepTime report={report}></SleepTime>
          <Score
            report={report}
            averageReport={averageReport}
            handleSetFactor={handleSetFactor}
          ></Score>
          <div className={styles.lottie}>
            {factor === 0 && <Coffee></Coffee>}
            {factor === 1 && <Walking></Walking>}
            {factor === 2 && <Meditation></Meditation>}
          </div>
        </>
      )}

      {!isLoading && !report.date && <Empty />}
    </div>
  );
}
