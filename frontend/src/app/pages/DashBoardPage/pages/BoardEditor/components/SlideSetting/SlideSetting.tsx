/**
 * Datart Vite
 *
 * Copyright 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { FC, memo, useContext, useMemo } from 'react';
import { useSelector } from 'react-redux';
import styled from 'styled-components';
import { BoardContext } from '../../../../components/BoardProvider/BoardProvider';
import { WidgetWrapProvider } from 'app/pages/DashBoardPage/components/WidgetProvider/WidgetWrapProvider';
import { selectSelectedIds } from '../../slice/selectors';
import { BoardConfigPanel } from './BoardConfigPanel';
import WidgetSetting from './WidgetSetting';

export const SlideSetting: FC<{}> = memo(() => {
  const { boardId } = useContext(BoardContext);
  const selectedIds = useSelector(selectSelectedIds);
  const {type,selectedIdArr} = useMemo(
    () => {
      const selectedIdArr = selectedIds ? selectedIds.split(',') : [];
      const type = selectedIdArr.length === 1 ? 'widget' : 'board';
      return { type, selectedIdArr };
    },
    [selectedIds],
  );
  return (
    <Wrapper>
      {type === 'board' && <BoardConfigPanel />}
      {type === 'widget' && (
        <WidgetWrapProvider
          id={selectedIdArr[0]}
          boardEditing={true}
          boardId={boardId}
        >
          <WidgetSetting boardId={boardId} />
        </WidgetWrapProvider>
      )}
    </Wrapper>
  );
});

export default SlideSetting;

const Wrapper = styled.div<{}>`
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: ${p => p.theme.componentBackground};
  box-shadow: ${p => p.theme.shadowSider};
  color: ${p => p.theme.textColorSnd};
`;
